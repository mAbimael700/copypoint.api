package com.copypoint.api.infra.mercadopago.manager;

import com.copypoint.api.domain.payment.entity.Payment;
import com.copypoint.api.domain.payment.entity.PaymentStatus;
import com.copypoint.api.domain.payment.dto.PaymentStatusResponse;
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
import java.util.Optional;

/**
 * Gestor responsable de manejar el estado de los pagos
 * Consulta estados, actualiza desde gateway y registra cambios
 */
@Component
public class PaymentStatusManager {
    private static final Logger logger = LoggerFactory.getLogger(PaymentStatusManager.class);

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private MercadoPagoGatewayService mercadoPagoGatewayService;

    @Autowired
    private PaymentAttemptService paymentAttemptService;

    public PaymentStatusResponse getPaymentStatus(Long paymentId) {
        try {
            Optional<Payment> paymentOpt = paymentService.findById(paymentId);
            if (paymentOpt.isEmpty()) {
                return buildErrorResponse("Pago no encontrado");
            }

            Payment payment = paymentOpt.get();

            // Si tiene gatewayId, consultar el estado en MercadoPago
            if (payment.getGatewayId() != null) {
                updatePaymentStatusFromGateway(payment);
            }

            return buildSuccessResponse(payment);

        } catch (Exception e) {
            logger.error("Error al consultar estado del pago {}: {}", paymentId, e.getMessage());
            return buildErrorResponse("Error al consultar el estado: " + e.getMessage());
        }
    }

    @Transactional
    public void updatePaymentStatusFromGateway(Payment payment) {
        try {
            PaymentStatus gatewayStatus = mercadoPagoGatewayService.getPaymentStatusFromGateway(payment.getGatewayId());

            if (gatewayStatus != null && gatewayStatus != payment.getStatus()) {
                PaymentStatus previousStatus = payment.getStatus();

                // Actualizar el estado del payment
                paymentService.updatePaymentStatus(payment.getId(), gatewayStatus);

                // Registrar la actualización de estado
                recordStatusUpdate(payment, previousStatus, gatewayStatus);

                logger.info("Payment {} actualizado de {} a {}",
                        payment.getId(), previousStatus, gatewayStatus);
            }

        } catch (Exception e) {
            logger.error("Error al actualizar estado desde MercadoPago para payment {}: {}",
                    payment.getId(), e.getMessage());
        }
    }

    private void recordStatusUpdate(Payment payment, PaymentStatus previousStatus, PaymentStatus newStatus) {
        try {
            var statusUpdateData = new StatusUpdateData(
                    previousStatus.name(),
                    newStatus.name(),
                    "STATUS_UPDATE_FROM_GATEWAY",
                    LocalDateTime.now().toString()
            );

            paymentAttemptService.createPaymentAttempt(
                    payment,
                    PaymentAttemptStatus.valueOf(newStatus.name()),
                    statusUpdateData
            );
        } catch (Exception e) {
            logger.error("Error al registrar actualización de estado: {}", e.getMessage());
        }
    }

    private PaymentStatusResponse buildSuccessResponse(Payment payment) {
        return new PaymentStatusResponse(
                payment.getId().toString(),
                payment.getStatus(),
                null,
                payment.getAmount(),
                payment.getCurrency(),
                null
        );
    }

    private PaymentStatusResponse buildErrorResponse(String errorMessage) {
        return new PaymentStatusResponse(null, null, null, null, null, errorMessage);
    }

    // Clase interna para datos de actualización de estado
    private static class StatusUpdateData {
        public final String previousStatus;
        public final String newStatus;
        public final String type;
        public final String timestamp;

        public StatusUpdateData(String previousStatus, String newStatus, String type, String timestamp) {
            this.previousStatus = previousStatus;
            this.newStatus = newStatus;
            this.type = type;
            this.timestamp = timestamp;
        }
    }
}
