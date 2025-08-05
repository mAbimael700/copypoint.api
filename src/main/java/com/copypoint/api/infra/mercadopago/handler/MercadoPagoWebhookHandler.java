package com.copypoint.api.infra.mercadopago.handler;


import com.copypoint.api.domain.payment.entity.Payment;
import com.copypoint.api.domain.payment.entity.PaymentStatus;
import com.copypoint.api.domain.payment.service.PaymentService;
import com.copypoint.api.domain.paymentattempt.entity.PaymentAttemptStatus;
import com.copypoint.api.domain.paymentattempt.service.PaymentAttemptService;
import com.copypoint.api.infra.mercadopago.service.MercadoPagoGatewayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Manejador especializado para webhooks de MercadoPago
 * Procesa notificaciones de cambios de estado y registra intentos
 */
@Component
public class MercadoPagoWebhookHandler {
    private static final Logger logger = LoggerFactory.getLogger(MercadoPagoWebhookHandler.class);

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private MercadoPagoGatewayService mercadoPagoGatewayService;

    @Autowired
    private PaymentAttemptService paymentAttemptService;

    @Transactional
    public void handleWebhook(String webHookPaymentId, String webHookStatus) {
        logger.info("Procesando webhook para payment ID: {}, status: {}", webHookPaymentId, webHookStatus);

        try {
            // Mapear el estado de MercadoPago a nuestro enum
            PaymentStatus newStatus = mercadoPagoGatewayService.mapMercadoPagoStatus(webHookStatus);

            // Actualizar el payment por gateway ID
            Payment payment = paymentService.updatePaymentStatusByGatewayId(webHookPaymentId, newStatus);

            // Registrar el intento de pago desde webhook
            recordWebhookAttempt(payment, webHookPaymentId, webHookStatus, newStatus);

            logger.info("Webhook procesado exitosamente para payment ID: {}", payment.getId());

        } catch (Exception e) {
            logger.error("Error procesando webhook para payment ID {}: {}", webHookPaymentId, e.getMessage());

            // Registrar el error del webhook
            recordWebhookError(webHookPaymentId, webHookStatus, e.getMessage());
        }
    }

    private void recordWebhookAttempt(Payment payment, String webHookPaymentId,
                                      String webHookStatus, PaymentStatus newStatus) {
        try {
            var webhookData = new WebhookData(
                    webHookPaymentId,
                    webHookStatus,
                    "WEBHOOK_NOTIFICATION",
                    LocalDateTime.now().toString()
            );

            PaymentAttemptStatus attemptStatus = mapToAttemptStatus(newStatus);

            paymentAttemptService.createPaymentAttempt(
                    payment,
                    attemptStatus,
                    webhookData
            );

        } catch (Exception e) {
            logger.error("Error al registrar intento de webhook: {}", e.getMessage());
        }
    }

    private void recordWebhookError(String webHookPaymentId, String webHookStatus, String errorMessage) {
        try {
            var errorData = new WebhookErrorData(
                    webHookPaymentId,
                    webHookStatus,
                    "WEBHOOK_ERROR",
                    errorMessage,
                    LocalDateTime.now().toString()
            );

            // Aquí podrías tener un servicio para registrar errores de webhook
            logger.error("Error de webhook registrado: {}", errorData);

        } catch (Exception e) {
            logger.error("Error al registrar error de webhook: {}", e.getMessage());
        }
    }

    private PaymentAttemptStatus mapToAttemptStatus(PaymentStatus paymentStatus) {
        try {
            return PaymentAttemptStatus.valueOf(paymentStatus.name());
        } catch (IllegalArgumentException e) {
            logger.warn("No se pudo mapear PaymentStatus {} a PaymentAttemptStatus, usando PENDING", paymentStatus);
            return PaymentAttemptStatus.PENDING;
        }
    }

    // Clases internas para datos de webhook
    private static class WebhookData {
        public final String paymentId;
        public final String status;
        public final String type;
        public final String timestamp;

        public WebhookData(String paymentId, String status, String type, String timestamp) {
            this.paymentId = paymentId;
            this.status = status;
            this.type = type;
            this.timestamp = timestamp;
        }
    }

    private static class WebhookErrorData {
        public final String paymentId;
        public final String status;
        public final String type;
        public final String errorMessage;
        public final String timestamp;

        public WebhookErrorData(String paymentId, String status, String type, String errorMessage, String timestamp) {
            this.paymentId = paymentId;
            this.status = status;
            this.type = type;
            this.errorMessage = errorMessage;
            this.timestamp = timestamp;
        }
    }
}
