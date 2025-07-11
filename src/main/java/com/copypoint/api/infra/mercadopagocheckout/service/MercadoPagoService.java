package com.copypoint.api.infra.mercadopagocheckout.service;

import com.copypoint.api.domain.payment.Payment;
import com.copypoint.api.domain.payment.PaymentStatus;
import com.copypoint.api.domain.payment.dto.PaymentRequest;
import com.copypoint.api.domain.payment.dto.PaymentResponse;
import com.copypoint.api.domain.payment.dto.PaymentStatusResponse;

import com.copypoint.api.domain.payment.service.PaymentService;

import com.copypoint.api.domain.paymentattempt.PaymentAttemptStatus;

import com.copypoint.api.domain.paymentattempt.service.PaymentAttemptService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class MercadoPagoService {

    private static final Logger logger = LoggerFactory.getLogger(MercadoPagoService.class);

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private MercadoPagoGatewayService mercadoPagoGatewayService;

    @Autowired
    private PaymentAttemptService paymentAttemptService;

    // Configure ObjectMapper with Java 8 time support
    private final ObjectMapper objectMapper;

    public MercadoPagoService() {
        this.objectMapper = new ObjectMapper();
        // Register the JavaTimeModule to handle Java 8 date/time types
        this.objectMapper.registerModule(new JavaTimeModule());
        // Disable writing dates as timestamps
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public PaymentResponse createPayment(PaymentRequest request) throws MPException, MPApiException {
        logger.info("Iniciando creación de pago para saleId: {}", request.saleId());

        Payment payment = null;

        try {
            // Paso 1: Crear el pago inicial en la base de datos
            payment = createInitialPayment(request);
            logger.info("Payment creado con ID: {}", payment.getId());

            // Paso 2: Crear la preferencia en MercadoPago (sin transacción)
            logger.info("Creando preferencia en MercadoPago...");
            Preference preference = mercadoPagoGatewayService.createPreference(payment, request);
            logger.info("Preferencia creada con ID: {}", preference.getId());

            // Paso 3: Actualizar el payment con los datos de MercadoPago
            payment = updatePaymentWithPreferenceData(payment, preference);
            logger.info("Payment actualizado con datos de MercadoPago");

            PaymentResponse response = new PaymentResponse(
                    true,
                    "Pago creado exitosamente",
                    preference.getInitPoint(),
                    preference.getId(),
                    payment.getId().toString(),
                    PaymentStatus.PENDING
            );

            logger.info("Proceso completado exitosamente");
            return response;

        } catch (IllegalArgumentException e) {
            logger.error("Error de validación: {}", e.getMessage());

            // Si ya se creó el payment, marcarlo como fallido
            if (payment != null) {
                markPaymentAsFailed(payment, "Error de validación: " + e.getMessage());
            }

            return new PaymentResponse(false, e.getMessage(), null, null, null, null);

        } catch (MPException | MPApiException e) {
            logger.error("Error de MercadoPago: {}", e.getMessage());

            // Si ya se creó el payment, marcarlo como fallido
            if (payment != null) {
                markPaymentAsFailed(payment, "Error de MercadoPago: " + e.getMessage());
            }

            return new PaymentResponse(false, "Error al crear el pago: " + e.getMessage(), null, null, null, null);

        } catch (Exception e) {
            logger.error("Error interno: {}", e.getMessage(), e);

            // Si ya se creó el payment, marcarlo como fallido
            if (payment != null) {
                markPaymentAsFailed(payment, "Error interno: " + e.getMessage());
            }

            return new PaymentResponse(false, "Error interno: " + e.getMessage(), null, null, null, null);
        }
    }

    @Transactional
    private Payment createInitialPayment(PaymentRequest request) {
        return paymentService.createPayment(
                request.saleId(),
                request.amount(),
                request.currency()
        );
    }

    @Transactional
    private Payment updatePaymentWithPreferenceData(Payment payment, Preference preference) {
        try {
            // Actualizar el payment con el ID de MercadoPago
            Payment updatedPayment = paymentService.updatePaymentGatewayId(
                    payment.getId(),
                    preference.getId()
            );

            // Crear registro de intento de pago usando el servicio especializado
            paymentAttemptService.createMercadoPagoPreferenceAttempt(updatedPayment, preference);

            return updatedPayment;

        } catch (Exception e) {
            logger.error("Error al actualizar payment con datos de preferencia: {}", e.getMessage());
            throw new RuntimeException("Error al procesar datos de MercadoPago", e);
        }
    }

    @Transactional
    private void markPaymentAsFailed(Payment payment, String errorMessage) {
        try {
            // Actualizar el estado del payment a fallido
            paymentService.updatePaymentStatus(payment.getId(), PaymentStatus.FAILED);

            // Crear registro de intento fallido usando el servicio especializado
            paymentAttemptService.createPaymentAttemptWithError(
                    payment,
                    PaymentAttemptStatus.FAILED,
                    "PAYMENT_CREATION_ERROR",
                    errorMessage
            );

            logger.info("Payment {} marcado como fallido", payment.getId());

        } catch (Exception e) {
            logger.error("Error al marcar payment como fallido: {}", e.getMessage());
            // No re-lanzar la excepción para no ocultar el error original
        }
    }

    public PaymentStatusResponse getPaymentStatus(Long paymentId) {
        try {
            Optional<Payment> paymentOpt = paymentService.findById(paymentId);
            if (paymentOpt.isEmpty()) {
                return new PaymentStatusResponse(null, null, null, null, null, "Pago no encontrado");
            }

            Payment payment = paymentOpt.get();

            // Si tiene gatewayId, consultar el estado en MercadoPago
            if (payment.getGatewayId() != null) {
                updatePaymentStatusFromGateway(payment);
            }

            return new PaymentStatusResponse(
                    payment.getId().toString(),
                    payment.getStatus(),
                    null,
                    payment.getAmount(),
                    payment.getCurrency(),
                    null
            );

        } catch (Exception e) {
            return new PaymentStatusResponse(null, null, null, null, null, "Error al consultar el estado: " + e.getMessage());
        }
    }

    public void handleWebhook(String webHookPaymentId, String webHookstatus) {
        try {
            PaymentStatus newStatus = mercadoPagoGatewayService.mapMercadoPagoStatus(webHookstatus);
            Payment payment = paymentService.updatePaymentStatusByGatewayId(webHookPaymentId, newStatus);

            // Crear registro de intento usando el servicio especializado
            var webhookData = new Object() {
                public final String paymentId = webHookPaymentId;
                public final String status = webHookstatus;
                public final String type = "WEBHOOK_NOTIFICATION";
                public final String timestamp = java.time.LocalDateTime.now().toString();
            };

            paymentAttemptService.createPaymentAttempt(
                    payment,
                    PaymentAttemptStatus.valueOf(newStatus.name()),
                    webhookData
            );

        } catch (Exception e) {
            logger.error("Error procesando webhook: {}", e.getMessage());
        }
    }

    private void updatePaymentStatusFromGateway(Payment payment) {
        try {
            PaymentStatus gatewayStatus = mercadoPagoGatewayService.getPaymentStatusFromGateway(payment.getGatewayId());

            if (gatewayStatus != null && gatewayStatus != payment.getStatus()) {
                paymentService.updatePaymentStatus(payment.getId(), gatewayStatus);

                // Registrar la actualización de estado
                var statusUpdateData = new Object() {
                    public final String previousStatus = payment.getStatus().name();
                    public final String newStatus = gatewayStatus.name();
                    public final String type = "STATUS_UPDATE_FROM_GATEWAY";
                    public final String timestamp = java.time.LocalDateTime.now().toString();
                };

                paymentAttemptService.createPaymentAttempt(
                        payment,
                        PaymentAttemptStatus.valueOf(gatewayStatus.name()),
                        statusUpdateData
                );
            }

        } catch (Exception e) {
            logger.error("Error al actualizar estado desde MercadoPago: {}", e.getMessage());
        }
    }

    public Payment getPaymentByGatewayId(String paymentId) {
        return paymentService.findByGatewayId(paymentId).get();
    }
}