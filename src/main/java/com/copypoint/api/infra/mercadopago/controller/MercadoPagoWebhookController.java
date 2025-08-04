package com.copypoint.api.infra.mercadopago.controller;

import com.copypoint.api.domain.mercadopagoconfiguration.service.MercadoPagoConfigurationService;
import com.copypoint.api.domain.payment.Payment;
import com.copypoint.api.domain.payment.service.PaymentService;
import com.copypoint.api.infra.mercadopago.service.MercadoPagoService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercadopago.client.merchantorder.MerchantOrderClient;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.resources.merchantorder.MerchantOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/webhook/mercadopago")
public class MercadoPagoWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(MercadoPagoWebhookController.class);

    @Autowired
    private MercadoPagoService mercadoPagoService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private MercadoPagoConfigurationService mercadoPagoConfigService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping
    public ResponseEntity<?> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("x-signature") String signature,
            @RequestParam("data.id") String dataId,
            @RequestParam(value = "type", required = false) String type) {

        try {
            logger.info("Webhook MercadoPago recibido - data.id: {}, type: {}", dataId, type);

            // Parsear el payload para obtener información adicional
            JsonNode payloadJson = objectMapper.readTree(payload);
            String topic = payloadJson.path("topic").asText();

            if (topic.isEmpty() && type != null) {
                topic = type; // Usar el parámetro type si topic no está en el payload
            }

            Payment payment = null;

            switch (topic){
                case "payment":
                    logger.info("Procesando webhook de payment con ID: {}", dataId);
                    payment = handlePaymentWebhook(dataId);
                    break;

                case "merchant_order":
                case "merchant_order_wh":
                case "topic_merchant_order_wh":
                    logger.info("Procesando webhook de merchant_order con ID: {}", dataId);
                    payment = handleMerchantOrderWebhook(dataId);
                    break;

                default:
                    logger.warn("Tipo de webhook desconocido: {}, intentando búsqueda genérica", topic);
                    // Fallback: buscar por cualquier ID
                    Optional<Payment> paymentOpt = paymentService.findByAnyGatewayId(dataId);
                    payment = paymentOpt.orElse(null);
                    break;
            }

            if (payment == null) {
                logger.warn("No se encontró payment para data.id: {}, topic: {}", dataId, topic);
                return ResponseEntity.notFound().build();
            }

            logger.info("Payment encontrado: ID={}, Status={}, IntentId={}, PaymentId={}",
                    payment.getId(), payment.getStatus(),
                    payment.getGatewayIntentId(), payment.getGatewayPaymentId());

            // Validar signature con el webhook secret de la configuración
            if (signature != null) {
                String webhookSecret = mercadoPagoConfigService
                        .getWebhookSecretForSale(payment.getSale());

                if (!validateWebhookSignature(payload, signature, webhookSecret)) {
                    logger.warn("Signature inválida para payment: {}", payment.getId());
                    return ResponseEntity.badRequest().build();
                }
            } else {
                logger.warn("No se recibió signature en el webhook");
            }

            // Procesar webhook
            boolean processed = mercadoPagoService.handleWebhook(dataId, topic, payload);

            if (processed) {
                logger.info("Webhook procesado exitosamente para payment: {}", payment.getId());
                return ResponseEntity.ok().build();
            } else {
                logger.warn("No se pudo procesar el webhook para payment: {}", payment.getId());
                return ResponseEntity.status(422).build(); // Unprocessable Entity
            }

        } catch (Exception e) {
            logger.error("Error procesando webhook MercadoPago para data.id {}: {}",
                    dataId, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Maneja webhooks de tipo 'payment'
     * Consulta el payment en MercadoPago para obtener el external_reference
     */
    private Payment handlePaymentWebhook(String mpPaymentId) {
        try {
            PaymentClient client = new PaymentClient();
            com.mercadopago.resources.payment.Payment mpPayment = client.get(Long.parseLong(mpPaymentId));

            String externalReference = mpPayment.getExternalReference();
            logger.info("Payment de MP consultado - ID: {}, External Ref: {}, Status: {}",
                    mpPayment.getId(), externalReference, mpPayment.getStatus());

            if (externalReference != null && !externalReference.trim().isEmpty()) {
                // El external_reference contiene nuestro Payment ID interno
                Long internalPaymentId = Long.parseLong(externalReference);
                Optional<Payment> paymentOpt = paymentService.findById(internalPaymentId);

                if (paymentOpt.isPresent()) {
                    Payment payment = paymentOpt.get();

                    // Actualizar el gatewayPaymentId si no lo tenemos
                    if (!payment.hasGatewayPaymentId()) {
                        paymentService.updatePaymentGatewayPaymentId(payment.getId(), mpPaymentId);
                        logger.info("Payment {} actualizado con Gateway Payment ID: {}",
                                payment.getId(), mpPaymentId);

                        // Recargar el payment con los datos actualizados
                        payment = paymentService.findById(internalPaymentId).orElse(payment);
                    }

                    return payment;
                }
            }

            return null;
        } catch (Exception e) {
            logger.error("Error obteniendo payment por MP payment ID {}: {}", mpPaymentId, e.getMessage());
            return null;
        }
    }

    /**
     * Maneja webhooks de tipo 'merchant_order'
     * Consulta la merchant order en MercadoPago para obtener el external_reference
     */
    private Payment handleMerchantOrderWebhook(String mpOrderId) {
        try {
            MerchantOrderClient client = new MerchantOrderClient();
            MerchantOrder merchantOrder = client.get(Long.parseLong(mpOrderId));

            String externalReference = merchantOrder.getExternalReference();
            logger.info("Merchant Order de MP consultado - ID: {}, External Ref: {}",
                    merchantOrder.getId(), externalReference);

            if (externalReference != null && !externalReference.trim().isEmpty()) {
                Long internalPaymentId = Long.parseLong(externalReference);
                Optional<Payment> paymentOpt = paymentService.findById(internalPaymentId);
                return paymentOpt.orElse(null);
            }

            return null;
        } catch (Exception e) {
            logger.error("Error obteniendo payment por MP order ID {}: {}", mpOrderId, e.getMessage());
            return null;
        }
    }

    /**
     * Valida la signature del webhook de MercadoPago
     * TODO: Implementar validación real usando el secret y el algoritmo de MP
     */
    private boolean validateWebhookSignature(String payload, String signature, String secret) {
        // Por ahora retorna true, pero aquí deberías implementar la validación real
        // Referencia: https://www.mercadopago.com.mx/developers/en/docs/webhooks/signature

        if (secret == null || secret.trim().isEmpty()) {
            logger.warn("No hay webhook secret configurado, saltando validación");
            return true;
        }

        // TODO: Implementar validación HMAC SHA256
        logger.debug("Validando signature del webhook (implementación pendiente)");
        return true;
    }
}
