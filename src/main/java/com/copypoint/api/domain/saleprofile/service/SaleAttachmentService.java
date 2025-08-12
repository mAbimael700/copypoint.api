package com.copypoint.api.domain.saleprofile.service;

import com.copypoint.api.domain.attachment.Attachment;
import com.copypoint.api.domain.attachment.AttachmentFileType;
import com.copypoint.api.domain.attachment.repository.AttachmentRepository;
import com.copypoint.api.domain.attachment.service.PageCountService;
import com.copypoint.api.domain.profile.Profile;
import com.copypoint.api.domain.profile.repository.ProfileRepository;
import com.copypoint.api.domain.sale.Sale;
import com.copypoint.api.domain.sale.SaleStatus;
import com.copypoint.api.domain.sale.repository.SaleRepository;
import com.copypoint.api.domain.saleprofile.SaleProfile;
import com.copypoint.api.domain.saleprofile.SaleProfileId;
import com.copypoint.api.domain.saleprofile.dto.SaleAttachmentDTO;
import com.copypoint.api.domain.saleprofile.dto.SaleProfileDTO;
import com.copypoint.api.domain.saleprofile.repository.SaleProfileRepository;
import com.copypoint.api.domain.service.repository.ServiceRepository;
import com.copypoint.api.infra.cloudflare.r2.service.CloudflareR2Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
public class SaleAttachmentService {

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private SaleProfileRepository saleProfileRepository;

    @Autowired
    private CloudflareR2Service cloudflareR2Service;

    @Autowired
    private PageCountService pageCountService;


    /**
     * Agrega un attachment a un item de venta existente o crea uno nuevo
     */
    @Transactional
    public SaleProfileDTO addAttachmentToSale(Long saleId, Long attachmentId, SaleAttachmentDTO attachmentDTO) {
        log.debug("Agregando attachment {} a venta {} con datos: {}", attachmentId, saleId, attachmentDTO);

        // Validar que la venta existe y está en estado PENDING
        Sale sale = validateSaleForModification(saleId);

        // Validar que el attachment existe y está disponible
        Attachment attachment = validateAttachment(attachmentId);

        // Calcular páginas del attachment si no está calculado
        calculateAttachmentPages(attachment);

        // Buscar o crear el SaleProfile
        SaleProfile saleProfile = findOrCreateSaleProfile(sale, attachmentDTO, attachment);

        // Asignar el attachment al SaleProfile
        saleProfile.setAttachment(attachment);

        // Si es un nuevo item, usar las páginas calculadas como cantidad por defecto
        if (saleProfile.getId() == null ||
                (attachmentDTO.useCalculatedQuantity() != null && attachmentDTO.useCalculatedQuantity())) {

            if (attachment.getPages() != null && attachment.getPages() > 0) {
                saleProfile.setQuantity(attachment.getPages() * attachmentDTO.copies());
                log.info("Estableciendo cantidad basada en páginas: {} páginas x {} copias = {} cantidad",
                        attachment.getPages(), attachmentDTO.copies(), saleProfile.getQuantity());
            }
        }

        // Recalcular subtotal
        saleProfile.setSubtotal(saleProfile.getUnitPrice() * saleProfile.getQuantity());

        // Guardar el SaleProfile
        SaleProfile savedSaleProfile = saleProfileRepository.save(saleProfile);

        // Actualizar el total de la venta
        updateSaleTotal(sale);

        log.info("Attachment {} agregado exitosamente al SaleProfile - Sale ID: {}, Profile ID: {}, Service ID: {}",
                attachmentId, saleId, attachmentDTO.profileId(), attachmentDTO.serviceId());

        return new SaleProfileDTO(savedSaleProfile);
    }

    /**
     * Remueve un attachment de un SaleProfile
     */
    @Transactional
    public SaleProfileDTO removeAttachmentFromSaleProfile(Long saleId, Long profileId, Long serviceId) {
        log.debug("Removiendo attachment de SaleProfile - Sale: {}, Profile: {}, Service: {}",
                saleId, profileId, serviceId);

        // Validar que la venta existe y está en estado PENDING
        Sale sale = validateSaleForModification(saleId);

        // Buscar el SaleProfile
        SaleProfileId saleProfileId = new SaleProfileId(saleId, profileId, serviceId);
        Optional<SaleProfile> saleProfileOpt = saleProfileRepository.findById(saleProfileId);

        if (saleProfileOpt.isEmpty()) {
            throw new RuntimeException("SaleProfile no encontrado - Sale ID: " + saleId +
                    ", Profile ID: " + profileId + ", Service ID: " + serviceId);
        }

        SaleProfile saleProfile = saleProfileOpt.get();

        // Remover la referencia al attachment
        saleProfile.setAttachment(null);

        // Guardar cambios
        SaleProfile savedSaleProfile = saleProfileRepository.save(saleProfile);

        log.info("Attachment removido exitosamente del SaleProfile - Sale ID: {}, Profile ID: {}, Service ID: {}",
                saleId, profileId, serviceId);

        return new SaleProfileDTO(savedSaleProfile);
    }

    /**
     * Obtiene información de un attachment asociado a un SaleProfile
     */
    @Transactional(readOnly = true)
    public Optional<Attachment> getAttachmentFromSaleProfile(Long saleId, Long profileId, Long serviceId) {
        SaleProfileId saleProfileId = new SaleProfileId(saleId, profileId, serviceId);
        Optional<SaleProfile> saleProfileOpt = saleProfileRepository.findById(saleProfileId);

        return saleProfileOpt.map(SaleProfile::getAttachment);
    }

    // Métodos privados de apoyo

    private Sale validateSaleForModification(Long saleId) {
        Optional<Sale> saleOpt = saleRepository.findById(saleId);
        if (saleOpt.isEmpty()) {
            log.error("Venta no encontrada con ID: {}", saleId);
            throw new RuntimeException("Venta no encontrada con ID: " + saleId);
        }

        Sale sale = saleOpt.get();
        if (sale.getStatus() != SaleStatus.PENDING) {
            log.warn("Intento de modificar venta en estado no válido. Sale ID: {}, Estado: {}",
                    saleId, sale.getStatus());
            throw new RuntimeException("Solo se pueden modificar ventas en estado PENDING. Estado actual: " +
                    sale.getStatus());
        }

        return sale;
    }

    private Attachment validateAttachment(Long attachmentId) {
        Optional<Attachment> attachmentOpt = attachmentRepository.findById(attachmentId);
        if (attachmentOpt.isEmpty()) {
            log.error("Attachment no encontrado con ID: {}", attachmentId);
            throw new RuntimeException("Attachment no encontrado con ID: " + attachmentId);
        }

        Attachment attachment = attachmentOpt.get();
        if (!attachment.isDownloaded()) {
            log.error("Attachment no está disponible para uso - ID: {}, Estado: {}",
                    attachmentId, attachment.getDownloadStatus());
            throw new RuntimeException("El attachment no está disponible para uso. Estado: " +
                    attachment.getDownloadStatus());
        }

        return attachment;
    }

    private void calculateAttachmentPages(Attachment attachment) {
        // Solo calcular si no tiene páginas calculadas
        if (attachment.getPages() == null || attachment.getPages() <= 0) {
            try {
                int calculatedPages = pageCountService.calculatePages(attachment);
                attachment.setPages(calculatedPages);
                attachment.setDateUpdated(LocalDateTime.now());
                attachmentRepository.save(attachment);

                log.info("Páginas calculadas para attachment {}: {} páginas",
                        attachment.getId(), calculatedPages);

            } catch (Exception e) {
                log.warn("Error calculando páginas para attachment {}: {}",
                        attachment.getId(), e.getMessage());
                // Establecer páginas por defecto según tipo de archivo
                attachment.setPages(getDefaultPagesForFileType(attachment.getFileType()));
                attachmentRepository.save(attachment);
            }
        }
    }

    private int getDefaultPagesForFileType(AttachmentFileType fileType) {
        return switch (fileType) {
            case IMAGE, PNG, JPG -> 1;
            case PDF, DOC, DOCX, DOCUMENT -> 1; // Por defecto, aunque debería calcularse
            default -> 1;
        };
    }

    private SaleProfile findOrCreateSaleProfile(Sale sale, SaleAttachmentDTO dto, Attachment attachment) {
        // Validar que el profile existe
        Optional<Profile> profileOpt = profileRepository.findById(dto.profileId());
        if (profileOpt.isEmpty()) {
            throw new RuntimeException("Profile no encontrado con ID: " + dto.profileId());
        }

        // Validar que el service existe
        Optional<com.copypoint.api.domain.service.Service> serviceOpt =
                serviceRepository.findById(dto.serviceId());
        if (serviceOpt.isEmpty()) {
            throw new RuntimeException("Service no encontrado con ID: " + dto.serviceId());
        }

        Profile profile = profileOpt.get();
        com.copypoint.api.domain.service.Service service = serviceOpt.get();

        // Buscar SaleProfile existente
        SaleProfileId saleProfileId = new SaleProfileId(sale.getId(), dto.profileId(), dto.serviceId());
        Optional<SaleProfile> existingSaleProfile = saleProfileRepository.findById(saleProfileId);

        if (existingSaleProfile.isPresent()) {
            log.debug("Usando SaleProfile existente");
            return existingSaleProfile.get();
        } else {
            // Crear nuevo SaleProfile
            log.debug("Creando nuevo SaleProfile");

            // Calcular precio unitario (considerando conversión de moneda)
            Double unitPrice = getConvertedUnitPrice(profile, sale);

            // Cantidad inicial: páginas * copias o valor por defecto
            int initialQuantity = (attachment.getPages() != null && attachment.getPages() > 0) ?
                    attachment.getPages() * dto.copies() : dto.copies();

            return SaleProfile.builder()
                    .id(saleProfileId)
                    .sale(sale)
                    .profile(profile)
                    .service(service)
                    .unitPrice(unitPrice)
                    .quantity(initialQuantity)
                    .subtotal(unitPrice * initialQuantity)
                    .attachment(attachment)
                    .build();
        }
    }

    private Double getConvertedUnitPrice(Profile profile, Sale sale) {
        // Este método debería implementar la misma lógica que en SaleProfileService
        // para conversión de moneda. Por ahora retornamos el precio base.
        return profile.getUnitPrice();
    }

    private void updateSaleTotal(Sale sale) {
        // Refrescar la entidad Sale desde la base de datos
        sale = saleRepository.findById(sale.getId()).orElseThrow();

        // Recalcular el total basado en todos los SaleProfiles
        Double total = saleProfileRepository.findByIdSaleId(sale.getId())
                .stream()
                .mapToDouble(SaleProfile::getSubtotal)
                .sum();

        // Aplicar descuento si existe
        if (sale.getDiscount() != null && sale.getDiscount() > 0) {
            total = Math.max(0, total - sale.getDiscount());
        }

        sale.setTotal(total);
        sale.setUpdatedAt(LocalDateTime.now());
        saleRepository.save(sale);

        log.debug("Total actualizado para venta {}: {}", sale.getId(), total);
    }
}
