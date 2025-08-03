package com.copypoint.api.infra.mercadopago.controller;

import com.copypoint.api.domain.mercadopagoconfiguration.service.MercadoPagoConfigurationService;
import com.copypoint.api.domain.payment.Payment;
import com.copypoint.api.infra.mercadopago.service.MercadoPagoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhook/mercadopago")
public class MercadoPagoWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(MercadoPagoWebhookController.class);

    @Autowired
    private MercadoPagoService mercadoPagoService;

    @Autowired
    private MercadoPagoConfigurationService mercadoPagoConfigService;

    @PostMapping
    public ResponseEntity<?> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("x-signature") String signature,
            @RequestParam("data.id") String paymentId) {

        try {
            // Obtener el payment y la sale para validar webhook
            Payment payment = mercadoPagoService.getPaymentByGatewayId(paymentId);
            if (payment == null) {
                return ResponseEntity.notFound().build();
            }

            // Validar signature con el webhook secret de la configuración
            String webhookSecret = mercadoPagoConfigService
                    .getWebhookSecretForSale(payment.getSale());

            if (!validateWebhookSignature(payload, signature, webhookSecret)) {
                return ResponseEntity.badRequest().build();
            }

            // Procesar webhook
            mercadoPagoService.handleWebhook(paymentId, "approved");

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error procesando webhook: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    private boolean validateWebhookSignature(String payload, String signature, String secret) {
        // Implementar validación de signature
        return true;
    }
}
