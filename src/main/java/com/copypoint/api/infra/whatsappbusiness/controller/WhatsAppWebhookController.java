package com.copypoint.api.infra.whatsappbusiness.controller;

import com.copypoint.api.domain.whatsappbussinessconfiguration.dto.WhatsAppWebhookDTO;
import com.copypoint.api.infra.whatsappbusiness.service.WhatsAppBusinessWebhookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhook/whatsapp")
public class WhatsAppWebhookController {
    private static final Logger logger = LoggerFactory.getLogger(WhatsAppWebhookController.class);

    @Autowired
    private WhatsAppBusinessWebhookService webhookService;

    // Verificación del webhook (requerido por WhatsApp)
    @GetMapping("/{customerServicePhoneId}")
    public ResponseEntity<String> verifyWebhook(
            @PathVariable Long customerServicePhoneId,
            @RequestParam(value = "hub.mode", required = false) String mode,
            @RequestParam(value = "hub.challenge", required = false) String challenge,
            @RequestParam(value = "hub.verify_token", required = false) String verifyToken) {

        logger.info("Verificación de webhook WhatsApp para teléfono: {}, mode: {}, verifyToken: {}",
                customerServicePhoneId, mode, verifyToken);

        if ("subscribe".equals(mode)) {
            boolean isValid = webhookService.verifyWebhookToken(customerServicePhoneId, verifyToken);
            if (isValid) {
                logger.info("Token de verificación válido para teléfono: {}", customerServicePhoneId);
                return ResponseEntity.ok(challenge);
            } else {
                logger.warn("Token de verificación inválido para teléfono: {}", customerServicePhoneId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token inválido");
            }
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Modo no soportado");
    }

    // Recepción de mensajes
    @PostMapping("/{customerServicePhoneId}")
    public ResponseEntity<String> receiveMessage(
            @PathVariable Long customerServicePhoneId,
            @RequestBody WhatsAppWebhookDTO webhookData,
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature) {

        try {
            logger.info("Mensaje recibido en webhook WhatsApp para teléfono: {}", customerServicePhoneId);
            logger.debug("Datos del webhook: {}", webhookData);

            // Procesar el mensaje recibido
            webhookService.processIncomingMessage(customerServicePhoneId, webhookData, signature);

            return ResponseEntity.ok("MESSAGE_RECEIVED");

        } catch (Exception e) {
            logger.error("Error procesando mensaje de webhook WhatsApp: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error procesando mensaje");
        }
    }
}
