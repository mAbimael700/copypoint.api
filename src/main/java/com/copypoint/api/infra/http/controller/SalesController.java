package com.copypoint.api.infra.http.controller;

import com.copypoint.api.domain.employee.repository.EmployeeRepository;
import com.copypoint.api.domain.permission.PermissionService;
import com.copypoint.api.domain.sale.SaleStatus;
import com.copypoint.api.domain.sale.dto.SaleCreationDTO;
import com.copypoint.api.domain.sale.dto.SaleDTO;
import com.copypoint.api.domain.sale.service.SaleService;
import com.copypoint.api.domain.saleprofile.dto.SaleProfileCreationDTO;
import com.copypoint.api.domain.saleprofile.service.SaleProfileService;
import com.copypoint.api.domain.user.User;
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

    @Autowired
    private SaleProfileService saleProfileService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PermissionService permissionService;

    /**
     * Crear una nueva venta en estado PENDING
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
     */
    @PostMapping("/{saleId}/profiles")
    public ResponseEntity<SaleDTO> addProfileToSale(
            @PathVariable Long saleId,
            @RequestBody SaleProfileCreationDTO saleProfileDTO) {

        try {
            SaleDTO updatedSale = saleProfileService.addProfileToSale(saleId, saleProfileDTO);
            return ResponseEntity.ok(updatedSale);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Actualizar el estado de una venta
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
     * Obtener ventas por copypoint con permisos contextuales
     */
    @GetMapping
    public ResponseEntity<Page<SaleDTO>> getAllSalesByCopypoint(@PathVariable Long copypointId,
                                                                Pageable pageable,
                                                                @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            User user = userPrincipal.getUser();

            // Obtener información de acceso para este copypoint
            PermissionService.CopypointAccessInfo accessInfo =
                    permissionService.getUserCopypointAccess(user, copypointId);

            // Verificar si tiene acceso al copypoint
            if (!accessInfo.hasAccess()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Determinar qué ventas mostrar según el rol
            Page<SaleDTO> sales = getSalesForUserRole(accessInfo, user, copypointId, pageable);

            return ResponseEntity.ok(sales);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtener ventas pendientes por copypoint
     */
    @GetMapping("/pending")
    public ResponseEntity<Page<SaleDTO>> getPendingSalesByCopypoint(@PathVariable Long copypointId,
                                                                    @AuthenticationPrincipal UserPrincipal userPrincipal,
                                                                    Pageable pageable) {
        try {
            User user = userPrincipal.getUser();

            // Obtener información de acceso para este copypoint
            PermissionService.CopypointAccessInfo accessInfo =
                    permissionService.getUserCopypointAccess(user, copypointId);

            // Verificar si tiene acceso al copypoint
            if (!accessInfo.hasAccess()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Determinar qué ventas pendientes mostrar según el rol
            Page<SaleDTO> pendingSales = getPendingSalesForUserRole(accessInfo, user, copypointId, pageable);

            return ResponseEntity.ok(pendingSales);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Endpoint adicional: Finalizar venta (cambiar de PENDING a ON_HOLD)
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


    /**
     * Obtiene las ventas apropiadas según el rol del usuario
     */
    private Page<SaleDTO> getSalesForUserRole(PermissionService.CopypointAccessInfo accessInfo,
                                              User user, Long copypointId, Pageable pageable) {

        // Si es cajero, solo ve sus propias ventas
        if (accessInfo.hasRole("COPYPOINT_CASHIER") &&
                !accessInfo.hasAnyRole("COPYPOINT_MANAGER", "STORE_MANAGER")) {
            return saleService.getSalesByUserAndCopypoint(user.getId(), copypointId, pageable);
        }

        // Si es manager (de copypoint o store), ve todas las ventas del copypoint
        if (accessInfo.hasAnyRole("COPYPOINT_MANAGER", "STORE_MANAGER")) {
            return saleService.getAllSalesByCopypoint(copypointId, pageable);
        }

        // Si no tiene ningún rol reconocido, denegar acceso
        throw new RuntimeException("Usuario no tiene rol autorizado para ver ventas");
    }

    /**
     * Obtiene las ventas pendientes apropiadas según el rol del usuario
     */
    private Page<SaleDTO> getPendingSalesForUserRole(PermissionService.CopypointAccessInfo accessInfo,
                                                     User user, Long copypointId, Pageable pageable) {

        // Si es cajero, solo ve sus propias ventas pendientes
        if (accessInfo.hasRole("COPYPOINT_CASHIER") &&
                !accessInfo.hasAnyRole("COPYPOINT_MANAGER", "STORE_MANAGER")) {
            return saleService.getPendingSalesByUserAndCopypoint(user.getId(), copypointId, pageable);
        }

        // Si es manager (de copypoint o store), ve todas las ventas pendientes del copypoint
        if (accessInfo.hasAnyRole("COPYPOINT_MANAGER", "STORE_MANAGER")) {
            return saleService.getPendingSalesByCopypoint(copypointId, pageable);
        }

        // Si no tiene ningún rol reconocido, denegar acceso
        throw new RuntimeException("Usuario no tiene rol autorizado para ver ventas pendientes");
    }
}
