package com.copypoint.api.domain.sale.service;

import com.copypoint.api.domain.copypoint.Copypoint;
import com.copypoint.api.domain.copypoint.CopypointRepository;
import com.copypoint.api.domain.paymentmethod.PaymentMethod;
import com.copypoint.api.domain.paymentmethod.PaymentMethodRepository;
import com.copypoint.api.domain.profile.Profile;
import com.copypoint.api.domain.profile.ProfileRepository;
import com.copypoint.api.domain.sale.Sale;
import com.copypoint.api.domain.sale.SaleStatus;
import com.copypoint.api.domain.sale.repository.SaleRepository;
import com.copypoint.api.domain.sale.dto.SaleCreationDTO;
import com.copypoint.api.domain.sale.dto.SaleDTO;
import com.copypoint.api.domain.saleprofile.SaleProfile;
import com.copypoint.api.domain.saleprofile.SaleProfileId;
import com.copypoint.api.domain.saleprofile.dto.SaleProfileCreationDTO;
import com.copypoint.api.domain.saleprofile.repository.SaleProfileRepository;
import com.copypoint.api.domain.user.User;
import com.copypoint.api.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class SaleService {
    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private SaleProfileRepository saleProfileRepository;

    @Autowired
    private CopypointRepository copypointRepository;


    /**
     * Crear una venta en estado PENDING para un copypoint específico
     */
    @Transactional
    public SaleDTO createSale(Long userId, Long copypointId, SaleCreationDTO creationDTO) {
        // Validar que el usuario existe
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado con ID: " + userId);
        }

        // Validar que el copypoint existe
        Optional<Copypoint> copypoint = copypointRepository.findById(copypointId);
        if (copypoint.isEmpty()) {
            throw new RuntimeException("Copypoint no encontrado con ID: " + copypointId);
        }

        // Validar que el método de pago existe
        Optional<PaymentMethod> paymentMethod = paymentMethodRepository.findById(creationDTO.paymentMethodId());
        if (paymentMethod.isEmpty()) {
            throw new RuntimeException("Método de pago no encontrado con ID: " + creationDTO.paymentMethodId());
        }

        // Crear la nueva venta
        Sale newSale = Sale.builder()
                .userVendor(user.get())
                .copypoint(copypoint.get())
                .paymentMethod(paymentMethod.get())
                .currency(creationDTO.currency())
                .status(SaleStatus.PENDING)
                .total(0.0)
                .discount(0.0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .saleProfiles(new ArrayList<>())
                .payments(new ArrayList<>())
                .build();

        Sale savedSale = saleRepository.save(newSale);
        return new SaleDTO(savedSale);
    }


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
            throw new RuntimeException("Profile no encontrado con ID: " + saleProfileDTO.profileId());
        }


        Profile profileEntity = profile.get();

        // Verificar si ya existe este profile en la venta
        SaleProfileId saleProfileId = new SaleProfileId(saleId, saleProfileDTO.profileId());
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
                    .profile(profileEntity)
                    .unitPrice(unitPrice)
                    .quantity(saleProfileDTO.quantity())
                    .subtotal(subtotal)
                    .build();

            saleProfileRepository.save(newSaleProfile);
        }

        // Recalcular el total de la venta
        updateSaleTotal(sale);

        // Actualizar timestamp
        sale.setUpdatedAt(LocalDateTime.now());
        Sale updatedSale = saleRepository.save(sale);

        return new SaleDTO(updatedSale);
    }

    /**
     * Actualiza el estado de una venta PENDING a otro estado
     */
    /**
     * Actualizar el estado de una venta PENDING a ON_HOLD
     */
    @Transactional
    public SaleDTO updateSaleStatus(Long saleId, SaleStatus newStatus) {
        Optional<Sale> saleOpt = saleRepository.findById(saleId);
        if (saleOpt.isEmpty()) {
            throw new RuntimeException("Venta no encontrada con ID: " + saleId);
        }

        Sale sale = saleOpt.get();

        // Validar transición de estado (puedes agregar más validaciones aquí)
        if (sale.getStatus() != SaleStatus.PENDING && newStatus == SaleStatus.ON_HOLD) {
            throw new RuntimeException("Solo se puede cambiar a ON_HOLD desde estado PENDING. Estado actual: " + sale.getStatus());
        }

        sale.setStatus(newStatus);
        sale.setUpdatedAt(LocalDateTime.now());

        Sale updatedSale = saleRepository.save(sale);

        return new SaleDTO(updatedSale);
    }

    /**
     * Obtener todas las ventas por copypoint (todos los estados)
     */
    @Transactional(readOnly = true)
    public Page<SaleDTO> getAllSalesByCopypoint(Long copypointId, Pageable pageable) {
        // Validar que el copypoint existe
        Optional<Copypoint> copypoint = copypointRepository.findById(copypointId);
        if (copypoint.isEmpty()) {
            throw new RuntimeException("Copypoint no encontrado con ID: " + copypointId);
        }

        Page<Sale> allSales = saleRepository.findByCopypoint_Id(copypointId, pageable);

        return allSales.map(SaleDTO::new);
    }


    /**
     * Obtener todas las ventas pendientes por copypoint
     */
    @Transactional(readOnly = true)
    public Page<SaleDTO> getPendingSalesByCopypoint(Long copypointId, Pageable pageable) {
        // Validar que el copypoint existe
        Optional<Copypoint> copypoint = copypointRepository.findById(copypointId);
        if (copypoint.isEmpty()) {
            throw new RuntimeException("Copypoint no encontrado con ID: " + copypointId);
        }

        Page<Sale> pendingSales = saleRepository.findByCopypoint_IdAndStatus(copypointId, SaleStatus.PENDING, pageable);

        return pendingSales
                .map(SaleDTO::new);
    }

    /**
     * Obtiene todas las ventas pendientes de un usuario específico
     */
    /*@Transactional(readOnly = true)
    public Page<SaleDTO> getPendingSalesByUser(Long userId) {
        Page<Sale> pendingSales = saleRepository.findByUserVendorIdAndStatus(userId, SaleStatus.PENDING);
        return pendingSales.map(SaleDTO::new);
    }*/

    /**
     * Obtiene una venta por ID
     */
    @Transactional(readOnly = true)
    public Optional<SaleDTO> getSaleById(Long saleId) {
        return saleRepository.findById(saleId)
                .map(SaleDTO::new);
    }

    /**
     * Elimina un perfil de una venta PENDING
     */
    @Transactional
    public SaleDTO removeProfileFromSale(Long saleId, Long profileId) {
        Optional<Sale> saleOptional = saleRepository.findById(saleId);
        if (saleOptional.isEmpty()) {
            throw new RuntimeException("Venta no encontrada");
        }

        Sale sale = saleOptional.get();
        if (!sale.getStatus().equals(SaleStatus.PENDING)) {
            throw new RuntimeException("Solo se pueden remover perfiles de ventas PENDING");
        }

        SaleProfileId saleProfileId = new SaleProfileId(saleId, profileId);
        Optional<SaleProfile> saleProfileOptional = saleProfileRepository.findById(saleProfileId);

        if (saleProfileOptional.isEmpty()) {
            throw new RuntimeException("El perfil no está asociado a esta venta");
        }

        saleProfileRepository.delete(saleProfileOptional.get());

        // Recalcular el total de la venta
        updateSaleTotal(sale);

        return new SaleDTO(sale);
    }

    /**
     * Recalcula el total de una venta basado en sus perfiles
     */
    private void updateSaleTotal(Sale sale) {
        Double total = sale.getSaleProfiles().stream()
                .mapToDouble(SaleProfile::getSubtotal)
                .sum();

        // Aplicar descuento si existe
        if (sale.getDiscount() != null && sale.getDiscount() > 0) {
            total = total - sale.getDiscount();
        }

        sale.setTotal(Math.max(0, total)); // Asegurarse de que el total no sea negativo
        sale.setUpdatedAt(LocalDateTime.now());

        //saleRepository.save(sale);
    }
}
