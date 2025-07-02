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
import com.copypoint.api.domain.saleprofile.repository.SaleProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
    }
}
