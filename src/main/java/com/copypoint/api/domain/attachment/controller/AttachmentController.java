package com.copypoint.api.domain.attachment.controller;

import com.copypoint.api.domain.attachment.Attachment;
import com.copypoint.api.domain.attachment.dto.AttachmentAvailabilityResponse;
import com.copypoint.api.domain.attachment.dto.AttachmentResponse;
import com.copypoint.api.domain.attachment.dto.PresignedUrlResponse;
import com.copypoint.api.domain.attachment.service.AttachmentMediaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/attachments")
public class AttachmentController {
    private AttachmentMediaService attachmentService;

    /**
     * Descargar un archivo attachment por su ID
     * Este endpoint retorna directamente el archivo para visualización/descarga
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadAttachment(@PathVariable Long id) {
        return attachmentService.downloadAttachmentFile(id);
    }

    /**
     * Obtener información del attachment (metadata)
     */
    @GetMapping("/{id}")
    public ResponseEntity<AttachmentResponse> getAttachmentInfo(@PathVariable Long id) {
        Optional<Attachment> attachmentOpt = attachmentService.findById(id);

        if (attachmentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Attachment attachment = attachmentOpt.get();

        AttachmentResponse response = new AttachmentResponse(
                attachment.getId(),
                attachment.getOriginalName(),
                attachment.getFileType(),
                attachment.getMessage().getId(),
                attachment.getMimeType(),
                attachment.getFileSizeBytes(),
                attachment.getDownloadStatus(),
                attachmentService.isAttachmentAvailable(id),
                attachment.getDateCreated(),
                attachment.getDateDownloaded()
        );


        return ResponseEntity.ok(response);
    }

    /**
     * Generar URL prefirmada para acceso directo (opcional)
     * Útil si quieres que el frontend acceda directamente a R2
     */
    @GetMapping("/{id}/presigned-url")
    public ResponseEntity<PresignedUrlResponse> generatePresignedUrl(
            @PathVariable Long id,
            @RequestParam(defaultValue = "60") int expirationMinutes) {

        Optional<String> urlOpt = attachmentService.generatePresignedUrl(id, expirationMinutes);

        if (urlOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        PresignedUrlResponse response = new PresignedUrlResponse(
                id,
                urlOpt.get(),
                expirationMinutes
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Verificar si un attachment está disponible
     */
    @GetMapping("/{id}/available")
    public ResponseEntity<AttachmentAvailabilityResponse> checkAvailability(@PathVariable Long id) {
        boolean isAvailable = attachmentService.isAttachmentAvailable(id);

        AttachmentAvailabilityResponse response = new AttachmentAvailabilityResponse(
                id,
                isAvailable
        );

        return ResponseEntity.ok(response);
    }
}
