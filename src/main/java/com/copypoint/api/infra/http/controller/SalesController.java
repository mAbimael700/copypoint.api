package com.copypoint.api.infra.http.controller;

import com.copypoint.api.domain.sale.SaleStatus;
import com.copypoint.api.domain.sale.dto.SaleCreationDTO;
import com.copypoint.api.domain.sale.dto.SaleDTO;
import com.copypoint.api.domain.sale.service.SaleService;
import com.copypoint.api.domain.saleprofile.dto.SaleProfileCreationDTO;
import com.copypoint.api.infra.http.userprincipal.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/copypoints/{copypointId}/sales")
public class SalesController {
    @Autowired
    private SaleService saleService;

    /**
     * Crear una nueva venta en estado PENDING
     * POST /api/copypoints/copypointId}/sales
     */
    @PostMapping
    public ResponseEntity<SaleDTO> createSale(
            @PathVariable Long copypointId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody SaleCreationDTO saleCreationDTO) {

        try {
            // Más limpio con método de conveniencia
            Long userId = userPrincipal.getUser().getId();
            SaleDTO createdSale = saleService.createSale(userId, copypointId, saleCreationDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdSale);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Agregar un profile a una venta PENDING
     * POST /api/copypoints/{copypointId}/sales/{saleId}/profiles
     */
    @PostMapping("/{saleId}/profiles")
    public ResponseEntity<SaleDTO> addProfileToSale(
            @PathVariable Long saleId,
            @RequestBody SaleProfileCreationDTO saleProfileDTO) {

        try {
            SaleDTO updatedSale = saleService.addProfileToSale(saleId, saleProfileDTO);
            return ResponseEntity.ok(updatedSale);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Actualizar el estado de una venta
     * PATCH /api/copypoints/{copypointId}/sales/{saleId}/status
     */
    @PatchMapping("/{saleId}/status")
    public ResponseEntity<SaleDTO> updateSaleStatus(
            @PathVariable Long saleId,
            @RequestParam SaleStatus status) {

        try {
            SaleDTO updatedSale = saleService.updateSaleStatus(saleId, status);
            return ResponseEntity.ok(updatedSale);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtener todas las ventas por copypoint (todos los estados)
     * GET /api/sales/copypoint/{copypointId}
     */
    @GetMapping
    public ResponseEntity<Page<SaleDTO>> getAllSalesByCopypoint(@PathVariable Long copypointId, Pageable pageable) {
        try {
            Page<SaleDTO> allSales = saleService.getAllSalesByCopypoint(copypointId, pageable);
            return ResponseEntity.ok(allSales);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtener ventas pendientes por copypoint
     * GET /api/sales/copypoint/{copypointId}/pending
     */
    @GetMapping("/pending")
    public ResponseEntity<Page<SaleDTO>> getPendingSalesByCopypoint(@PathVariable Long copypointId, Pageable pageable) {
        try {
            Page<SaleDTO> pendingSales = saleService.getPendingSalesByCopypoint(copypointId, pageable);
            return ResponseEntity.ok(pendingSales);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Endpoint adicional: Finalizar venta (cambiar de PENDING a ON_HOLD)
     * PATCH /api/sales/{saleId}/hold
     */
    @PatchMapping("/{saleId}/hold")
    public ResponseEntity<SaleDTO> holdSale(@PathVariable Long saleId) {
        try {
            SaleDTO updatedSale = saleService.updateSaleStatus(saleId, SaleStatus.ON_HOLD);
            return ResponseEntity.ok(updatedSale);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
