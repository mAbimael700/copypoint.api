package com.copypoint.api.domain.saleprofile.controller;

import com.copypoint.api.domain.attachment.Attachment;
import com.copypoint.api.domain.attachment.dto.AttachmentResponse;
import com.copypoint.api.domain.saleprofile.dto.SaleAttachmentDTO;
import com.copypoint.api.domain.saleprofile.dto.SaleProfileDTO;
import com.copypoint.api.domain.saleprofile.service.SaleAttachmentService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/sales/{saleId}/attachment")
//@Tag(name = "Sale Attachments", description = "Gestión de attachments en ventas")
public class SaleAttachmentController {
    @Autowired
    private SaleAttachmentService saleAttachmentService;

    @PostMapping("/{attachmentId}")
   /* @Operation(
            summary = "Agregar attachment a una venta",
            description = "Agrega un attachment a un item de venta (SaleProfile) existente o crea uno nuevo. " +
                    "Calcula automáticamente las páginas del documento y puede usar este valor para la cantidad."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attachment agregado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Venta o attachment no encontrado"),
            @ApiResponse(responseCode = "409", description = "La venta no está en estado PENDING"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })*/
    public ResponseEntity<SaleProfileDTO> addAttachmentToSale(
            //@Parameter(description = "ID de la venta", required = true)
            @PathVariable Long saleId,

            //@Parameter(description = "ID del attachment", required = true)
            @PathVariable Long attachmentId,

            //@Parameter(description = "Datos para agregar el attachment", required = true)
            @Valid @RequestBody SaleAttachmentDTO attachmentDTO) {

        try {
            log.info("Agregando attachment {} a venta {} con datos: {}", attachmentId, saleId, attachmentDTO);

            SaleProfileDTO result = saleAttachmentService.addAttachmentToSale(saleId, attachmentId, attachmentDTO);

            log.info("Attachment agregado exitosamente - SaleProfile ID: sale {} profile {}", result.saleId(), result.profileId());
            return ResponseEntity.ok(result);

        } catch (RuntimeException e) {
            log.error("Error agregando attachment {} a venta {}: {}", attachmentId, saleId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error inesperado agregando attachment {} a venta {}: {}", attachmentId, saleId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/profile/{profileId}/service/{serviceId}")
    /*@Operation(
            summary = "Remover attachment de un item de venta",
            description = "Remueve la referencia al attachment de un SaleProfile específico"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attachment removido exitosamente"),
            @ApiResponse(responseCode = "404", description = "SaleProfile no encontrado"),
            @ApiResponse(responseCode = "409", description = "La venta no está en estado PENDING"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })*/
    public ResponseEntity<SaleProfileDTO> removeAttachmentFromSaleProfile(
            //@Parameter(description = "ID de la venta", required = true)
            @PathVariable Long saleId,

            //@Parameter(description = "ID del profile", required = true)
            @PathVariable Long profileId,

            //@Parameter(description = "ID del service", required = true)
            @PathVariable Long serviceId) {

        try {
            log.info("Removiendo attachment de SaleProfile - Sale: {}, Profile: {}, Service: {}",
                    saleId, profileId, serviceId);

            SaleProfileDTO result = saleAttachmentService.removeAttachmentFromSaleProfile(saleId, profileId, serviceId);

            log.info("Attachment removido exitosamente");
            return ResponseEntity.ok(result);

        } catch (RuntimeException e) {
            log.error("Error removiendo attachment: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error inesperado removiendo attachment: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/profile/{profileId}/service/{serviceId}")
    /*@Operation(
            summary = "Obtener attachment de un item de venta",
            description = "Obtiene el attachment asociado a un SaleProfile específico"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attachment encontrado"),
            @ApiResponse(responseCode = "404", description = "SaleProfile o attachment no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })*/
    public ResponseEntity<?> getAttachmentFromSaleProfile(
            //@Parameter(description = "ID de la venta", required = true)
            @PathVariable Long saleId,

            //@Parameter(description = "ID del profile", required = true)
            @PathVariable Long profileId,

            //@Parameter(description = "ID del service", required = true)
            @PathVariable Long serviceId) {

        try {
            Optional<Attachment> attachment = saleAttachmentService
                    .getAttachmentFromSaleProfile(saleId, profileId, serviceId);

            if (attachment.isPresent()) {
                return ResponseEntity.ok(new AttachmentResponse(attachment.get()));
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            log.error("Error obteniendo attachment de SaleProfile: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{attachmentId}/calculate-pages")
    /*@Operation(
            summary = "Forzar cálculo de páginas de un attachment",
            description = "Fuerza el recálculo de páginas de un attachment específico"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Páginas calculadas exitosamente"),
            @ApiResponse(responseCode = "404", description = "Attachment no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error calculando páginas")
    })*/
    public ResponseEntity<Integer> calculateAttachmentPages(
            //@Parameter(description = "ID de la venta", required = true)
            @PathVariable Long saleId,

            //@Parameter(description = "ID del attachment", required = true)
            @PathVariable Long attachmentId) {

        try {
            // Este endpoint podría implementarse si se necesita recalcular páginas manualmente
            // Por ahora retornamos NOT_IMPLEMENTED
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();

        } catch (Exception e) {
            log.error("Error calculando páginas del attachment {}: {}", attachmentId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
