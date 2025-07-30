package com.copypoint.api.infra.http.controller;

import com.copypoint.api.domain.sale.dto.SaleDTO;
import com.copypoint.api.domain.saleprofile.dto.SaleProfileCreationDTO;
import com.copypoint.api.domain.saleprofile.dto.SaleProfileDTO;
import com.copypoint.api.domain.saleprofile.dto.SaleProfileUpdateDTO;
import com.copypoint.api.domain.saleprofile.service.SaleProfileService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/copypoints/{copypointId}/sales")
public class SaleProfileController {

    @Autowired
    private SaleProfileService saleProfileService;

    /**
     * Agregar un profile a una venta PENDING
     */
    @PostMapping("/{saleId}/profiles")
    public ResponseEntity<SaleDTO> addProfileToSale(
            @PathVariable Long saleId,
            @Valid @RequestBody SaleProfileCreationDTO saleProfileDTO) {

        SaleDTO updatedSale = saleProfileService.addProfileToSale(saleId, saleProfileDTO);
        return ResponseEntity.ok(updatedSale);
    }

    @PatchMapping("/{saleId}/profiles")
    public ResponseEntity<SaleDTO> updateSaleProfile(
            @PathVariable Long saleId,
            @RequestParam Long profileId,
            @RequestParam Long serviceId,
            @Valid @RequestBody SaleProfileUpdateDTO profileUpdate) {

        SaleDTO updatedSale = saleProfileService.updateProfileInSale(saleId, profileId, serviceId, profileUpdate);
        return ResponseEntity.ok(updatedSale);
    }

    /**
     * Agregar un profile a una venta PENDING
     */
    @GetMapping("/{saleId}/profiles")
    public ResponseEntity<Page<SaleProfileDTO>> getProfilesBySale(
            @PathVariable Long saleId,
            Pageable pageable
    ) {
        try {
            Page<SaleProfileDTO> updatedSale = saleProfileService.getBySaleId(saleId, pageable);
            return ResponseEntity.ok(updatedSale);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
