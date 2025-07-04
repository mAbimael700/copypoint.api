package com.copypoint.api.domain.saleprofile.service;

import com.copypoint.api.domain.profile.Profile;
import com.copypoint.api.domain.profile.ProfileRepository;
import com.copypoint.api.domain.sale.Sale;
import com.copypoint.api.domain.sale.SaleStatus;
import com.copypoint.api.domain.sale.dto.SaleDTO;
import com.copypoint.api.domain.sale.repository.SaleRepository;
import com.copypoint.api.domain.saleprofile.SaleProfile;
import com.copypoint.api.domain.saleprofile.SaleProfileId;
import com.copypoint.api.domain.saleprofile.dto.SaleProfileCreationDTO;
import com.copypoint.api.domain.saleprofile.dto.SaleProfileDTO;
import com.copypoint.api.domain.saleprofile.dto.SaleProfileUpdateDTO;
import com.copypoint.api.domain.saleprofile.repository.SaleProfileRepository;
import com.copypoint.api.domain.service.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class SaleProfileService {

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private SaleProfileRepository saleProfileRepository;

    /**
     * Agregar un profile a una venta PENDING
     */
    @Transactional
    public SaleDTO addProfileToSale(Long saleId, SaleProfileCreationDTO saleProfileDTO) {
        // Validar que la venta existe y está en estado PENDING
        Optional<Sale> saleOpt = saleRepository.findById(saleId);
        if (saleOpt.isEmpty()) {
            throw new RuntimeException("Venta no encontrada con ID: " + saleId);
        }

        Sale sale = saleOpt.get();
        if (sale.getStatus() != SaleStatus.PENDING) {
            throw new RuntimeException("Solo se pueden agregar productos a ventas en estado PENDING. Estado actual: " + sale.getStatus());
        }

        // Validar que el profile existe
        Optional<Profile> profile = profileRepository.findById(saleProfileDTO.profileId());
        if (profile.isEmpty()) {
            throw new RuntimeException("Profile not found with ID: " + saleProfileDTO.profileId());
        }
        Optional<com.copypoint.api.domain.service.Service> service = serviceRepository.findById(saleProfileDTO.serviceId());
        if (service.isEmpty()) {
            throw new RuntimeException("Service not found with ID: " + saleProfileDTO.serviceId());
        }

        Profile profileEntity = profile.get();
        com.copypoint.api.domain.service.Service serviceEntity = service.get();

        // Verificar si ya existe este profile en la venta
        SaleProfileId saleProfileId = new SaleProfileId(saleId, saleProfileDTO.profileId(), saleProfileDTO.serviceId());
        Optional<SaleProfile> existingSaleProfile = saleProfileRepository.findById(saleProfileId);

        if (existingSaleProfile.isPresent()) {
            // Si ya existe, actualizar la cantidad y subtotal
            SaleProfile saleProfile = existingSaleProfile.get();
            saleProfile.setQuantity(saleProfile.getQuantity() + saleProfileDTO.quantity());
            saleProfile.setSubtotal(saleProfile.getQuantity() * saleProfile.getUnitPrice());
            saleProfileRepository.save(saleProfile);
        } else {
            // Si no existe, crear nuevo SaleProfile
            Double unitPrice = profileEntity.getUnitPrice();
            Double subtotal = unitPrice * saleProfileDTO.quantity();

            SaleProfile newSaleProfile = SaleProfile.builder()
                    .id(saleProfileId)
                    .sale(sale)
                    .service(serviceEntity)
                    .profile(profileEntity)
                    .unitPrice(unitPrice)
                    .quantity(saleProfileDTO.quantity())
                    .subtotal(subtotal)
                    .build();

            saleProfileRepository.save(newSaleProfile);
        }

        // SOLUCIÓN: Refrescar la entidad Sale desde la base de datos
        // para asegurar que tiene los SaleProfiles más actualizados
        sale = saleRepository.findById(saleId).orElseThrow();

        // Recalcular el total de la venta
        updateSaleTotal(sale);

        // Actualizar timestamp y guardar
        sale.setUpdatedAt(LocalDateTime.now());
        Sale updatedSale = saleRepository.save(sale);

        return new SaleDTO(updatedSale);
    }

    public Page<SaleProfileDTO> getBySaleId(Long saleId, Pageable pageable) {
        return saleProfileRepository.findByIdSaleId(saleId, pageable).map(SaleProfileDTO::new);
    }

    /**
     * Modificar la cantidad de un profile específico en una venta PENDING
     */
    @Transactional
    public SaleDTO updateProfileInSale(Long saleId, Long profileId, Long serviceId, Integer newQuantity) {
        // Validar que la venta existe y está en estado PENDING
        Optional<Sale> saleOpt = saleRepository.findById(saleId);
        if (saleOpt.isEmpty()) {
            throw new RuntimeException("Venta no encontrada con ID: " + saleId);
        }

        Sale sale = saleOpt.get();
        if (sale.getStatus() != SaleStatus.PENDING) {
            throw new RuntimeException("Solo se pueden modificar productos en ventas en estado PENDING. Estado actual: " + sale.getStatus());
        }

        // Validar que la cantidad es positiva
        if (newQuantity <= 0) {
            throw new RuntimeException("La cantidad debe ser mayor a 0");
        }

        // Buscar el SaleProfile existente
        SaleProfileId saleProfileId = new SaleProfileId(saleId, profileId, serviceId);
        Optional<SaleProfile> existingSaleProfile = saleProfileRepository.findById(saleProfileId);

        if (existingSaleProfile.isEmpty()) {
            throw new RuntimeException("Profile no encontrado en la venta. Sale ID: " + saleId + ", Profile ID: " + profileId);
        }

        // Actualizar la cantidad y subtotal
        SaleProfile saleProfile = existingSaleProfile.get();
        saleProfile.setQuantity(newQuantity);
        saleProfile.setSubtotal(saleProfile.getUnitPrice() * newQuantity);
        saleProfileRepository.save(saleProfile);

        // Refrescar la entidad Sale desde la base de datos
        sale = saleRepository.findById(saleId).orElseThrow();

        // Recalcular el total de la venta
        updateSaleTotal(sale);

        // Actualizar timestamp y guardar
        sale.setUpdatedAt(LocalDateTime.now());
        Sale updatedSale = saleRepository.save(sale);

        return new SaleDTO(updatedSale);
    }

    /**
     * Versión alternativa usando DTO para mayor consistencia
     */
    @Transactional
    public SaleDTO updateProfileInSale(Long saleId, Long profileId, Long serviceId, SaleProfileUpdateDTO updateDTO) {
        return updateProfileInSale(saleId, profileId, serviceId, updateDTO.quantity());
    }

    /**
     * Recalcula el total de una venta basado en sus perfiles
     */
    private void updateSaleTotal(Sale sale) {
        // OPCIÓN 1: Usar query directa (más confiable)
        Double total = saleProfileRepository.findByIdSaleId(sale.getId())
                .stream()
                .mapToDouble(SaleProfile::getSubtotal)
                .sum();

        // Aplicar descuento si existe
        if (sale.getDiscount() != null && sale.getDiscount() > 0) {
            total = total - sale.getDiscount();
        }

        sale.setTotal(Math.max(0, total)); // Asegurarse de que el total no sea negativo
        sale.setUpdatedAt(LocalDateTime.now());
    }
}