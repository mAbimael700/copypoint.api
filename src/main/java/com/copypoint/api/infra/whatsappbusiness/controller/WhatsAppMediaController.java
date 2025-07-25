package com.copypoint.api.infra.whatsappbusiness.controller;

import com.copypoint.api.infra.whatsappbusiness.service.WhatsAppMediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/whatsapp/media")
public class WhatsAppMediaController {

    @Autowired
    private WhatsAppMediaService whatsAppMediaService;

    /**
     * Verifica si un media está disponible
     */
    @GetMapping("/status/{phoneId}/{mediaId}")
    public ResponseEntity<?> checkMediaStatus(@PathVariable Long phoneId,
                                              @PathVariable String mediaId) {
        try {
            boolean available = whatsAppMediaService.isMediaAvailable(mediaId, phoneId);

            Map<String, Object> response = new HashMap<>();
            response.put("mediaId", mediaId);
            response.put("phoneId", phoneId);
            response.put("available", available);

            if (available) {
                response.put("downloadUrl", whatsAppMediaService.getMediaDownloadUrl(mediaId, phoneId));
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error verificando estado del media: " + e.getMessage());

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Reintenta la descarga de un media
     */
    @PostMapping("/retry/{phoneId}/{mediaId}")
    public ResponseEntity<?> retryMediaDownload(@PathVariable Long phoneId,
                                                @PathVariable String mediaId,
                                                @RequestParam Long messageId) {
        try {
            boolean success = whatsAppMediaService.retryMediaDownload(mediaId, phoneId, messageId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("mediaId", mediaId);
            response.put("message", success ? "Descarga reiniciada exitosamente" : "Falló el reintento de descarga");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error reintentando descarga: " + e.getMessage());

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Obtiene la URL de descarga directa
     */
    @GetMapping("/download/{phoneId}/{mediaId}")
    public ResponseEntity<?> getMediaDownloadUrl(@PathVariable Long phoneId,
                                                 @PathVariable String mediaId) {
        try {
            String downloadUrl = whatsAppMediaService.getMediaDownloadUrl(mediaId, phoneId);

            if (downloadUrl != null) {
                // Redirigir directamente al archivo en R2
                return ResponseEntity.status(302)
                        .header("Location", downloadUrl)
                        .build();
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Media no disponible");
                response.put("mediaId", mediaId);

                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error obteniendo URL de descarga: " + e.getMessage());

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
