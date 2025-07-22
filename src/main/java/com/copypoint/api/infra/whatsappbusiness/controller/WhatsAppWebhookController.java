package com.copypoint.api.infra.whatsappbusiness.controller;

import com.copypoint.api.infra.whatsappbusiness.dto.response.WhatsAppWebhookDTO;
import com.copypoint.api.infra.whatsappbusiness.service.WhatsAppBusinessWebhookService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.util.Map;

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

    @PostMapping("/{customerServicePhoneId}")
    public ResponseEntity<String> receiveMessage(
            @PathVariable Long customerServicePhoneId,
            @RequestBody(required = false) String rawBody,  // Capturar el JSON crudo primero
            @RequestHeader Map<String, String> headers,     // Capturar todos los headers
            HttpServletRequest request) {

        logger.info("=== POST REQUEST RECIBIDO ===");
        logger.info("Teléfono ID: {}", customerServicePhoneId);
        logger.info("Method: {}", request.getMethod());
        logger.info("URL: {}", request.getRequestURL());
        logger.info("Content-Type: {}", request.getContentType());

        // Log de headers importantes
        logger.info("Headers recibidos:");
        headers.forEach((key, value) -> {
            if (key.toLowerCase().contains("signature") ||
                    key.toLowerCase().contains("content") ||
                    key.toLowerCase().contains("user-agent")) {
                logger.info("  {}: {}", key, value);
            }
        });

        // Log del cuerpo crudo
        logger.info("Raw Body: {}", rawBody);

        if (rawBody == null || rawBody.trim().isEmpty()) {
            logger.warn("Cuerpo de la petición está vacío");
            return ResponseEntity.badRequest().body("Empty body");
        }

        try {
            // Intentar parsear manualmente el JSON
            ObjectMapper objectMapper = new ObjectMapper();
            WhatsAppWebhookDTO webhookData = objectMapper.readValue(rawBody, WhatsAppWebhookDTO.class);

            logger.info("JSON parseado exitosamente: {}", webhookData);

            String signature = headers.get("X-Hub-Signature-256");
            logger.info("Signature: {}", signature);

            // Procesar el mensaje recibido
            webhookService.processIncomingMessage(customerServicePhoneId, webhookData, signature);

            logger.info("Mensaje procesado exitosamente");
            return ResponseEntity.ok("MESSAGE_RECEIVED");

        } catch (JsonProcessingException e) {
            logger.error("Error parseando JSON: {}", e.getMessage());
            logger.error("JSON problemático: {}", rawBody);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error parsing JSON: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error procesando mensaje de webhook WhatsApp: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error procesando mensaje: " + e.getMessage());
        }
    }

    // Endpoint adicional para debugging - temporal
    @PostMapping("/{customerServicePhoneId}/debug")
    public ResponseEntity<String> debugMessage(
            @PathVariable Long customerServicePhoneId,
            HttpServletRequest request,
            @RequestHeader Map<String, String> headers) {

        logger.info("=== DEBUG ENDPOINT ===");
        logger.info("Método: {}", request.getMethod());
        logger.info("URL: {}", request.getRequestURL());

        try {
            // Leer el cuerpo manualmente
            StringBuilder body = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    body.append(line);
                }
            }

            logger.info("Headers completos:");
            headers.forEach((key, value) -> logger.info("  {}: {}", key, value));

            logger.info("Cuerpo completo: {}", body.toString());

            return ResponseEntity.ok("DEBUG_OK - Check logs");

        } catch (Exception e) {
            logger.error("Error en debug: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error en debug");
        }
    }
}
