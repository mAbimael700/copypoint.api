package com.copypoint.api.infra.mercadopago.orchestrator;

import com.copypoint.api.domain.payment.entity.Payment;
import com.copypoint.api.domain.payment.entity.PaymentStatus;
import com.copypoint.api.domain.payment.dto.PaymentRequest;
import com.copypoint.api.domain.payment.dto.PaymentGatewayResponse;
import com.copypoint.api.domain.payment.service.PaymentService;
import com.copypoint.api.domain.paymentattempt.entity.PaymentAttemptStatus;
import com.copypoint.api.domain.paymentattempt.service.PaymentAttemptService;
import com.copypoint.api.infra.mercadopago.service.MercadoPagoGatewayService;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orquestador responsable de coordinar la creación de pagos
 * Maneja el flujo completo: crear pago -> crear preferencia -> actualizar payment
 */
@Component
public class PaymentCreationOrchestrator {

    private static final Logger logger = LoggerFactory.getLogger(PaymentCreationOrchestrator.class);

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private MercadoPagoGatewayService mercadoPagoGatewayService;

    @Autowired
    private PaymentAttemptService paymentAttemptService;

    public PaymentGatewayResponse createPayment(PaymentRequest request) throws MPException, MPApiException {
        logger.info("Iniciando creación de pago para saleId: {}", request.saleId());

        Payment payment = null;

        try {
            // Paso 1: Crear el pago inicial en la base de datos
            payment = paymentService.createPayment(request, "Digital Wallet");
            logger.info("Payment creado con ID: {}", payment.getId());

            // Paso 2: Crear la preferencia en MercadoPago
            logger.info("Creando preferencia en MercadoPago...");
            Preference preference = mercadoPagoGatewayService.createPreference(payment, request);
            logger.info("Preferencia creada con ID: {}", preference.getId());

            // Paso 3: Actualizar el payment con los datos de MercadoPago
            payment = updatePaymentWithPreferenceData(payment, preference);
            logger.info("Payment actualizado con datos de MercadoPago");

            PaymentGatewayResponse response = buildSuccessResponse(preference, payment);
            logger.info("Proceso completado exitosamente");
            return response;

        } catch (IllegalArgumentException e) {
            logger.error("Error de validación: {}", e.getMessage());
            markPaymentAsFailed(payment, "Error de validación: " + e.getMessage());
            return buildErrorResponse(e.getMessage());

        } catch (MPException | MPApiException e) {
            logger.error("Error de MercadoPago: {}", e.getMessage());
            markPaymentAsFailed(payment, "Error de MercadoPago: " + e.getMessage());
            return buildErrorResponse("Error al crear el pago: " + e.getMessage());

        } catch (Exception e) {
            logger.error("Error interno: {}", e.getMessage(), e);
            markPaymentAsFailed(payment, "Error interno: " + e.getMessage());
            return buildErrorResponse("Error interno: " + e.getMessage());
        }
    }


    @Transactional
    private Payment updatePaymentWithPreferenceData(Payment payment, Preference preference) {
        try {
            // Actualizar el payment con el Intent ID (Preference ID en MercadoPago)
            Payment updatedPayment = paymentService.updatePaymentGatewayIntentId(
                    payment.getId(),
                    preference.getId()
            );

            // Crear registro de intento de pago usando el servicio especializado
            paymentAttemptService.createMercadoPagoPreferenceAttempt(updatedPayment, preference);

            logger.info("Payment {} actualizado con Gateway Intent ID (Preference): {}",
                    updatedPayment.getId(), preference.getId());

            return updatedPayment;

        } catch (Exception e) {
            logger.error("Error al actualizar payment con datos de preferencia: {}", e.getMessage());
            throw new RuntimeException("Error al procesar datos de MercadoPago", e);
        }
    }

    @Transactional
    private void markPaymentAsFailed(Payment payment, String errorMessage) {
        if (payment == null) {
            return;
        }

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

    // También actualizar el método buildSuccessResponse si es necesario
    private PaymentGatewayResponse buildSuccessResponse(Preference preference, Payment payment) {
        return new PaymentGatewayResponse(
                true,
                "Pago creado exitosamente",
                preference.getInitPoint(),
                preference.getId(), // Este es el gatewayIntentId
                payment.getId().toString(),
                PaymentStatus.PENDING
        );
    }

    private PaymentGatewayResponse buildErrorResponse(String errorMessage) {
        return new PaymentGatewayResponse(false, errorMessage, null, null, null, null);
    }
}
