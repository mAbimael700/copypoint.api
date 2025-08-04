package com.copypoint.api.infra.mercadopago.service;

import com.copypoint.api.domain.payment.Payment;
import com.copypoint.api.domain.payment.PaymentStatus;
import com.copypoint.api.domain.payment.dto.PaymentRequest;
import com.copypoint.api.domain.payment.dto.PaymentGatewayResponse;
import com.copypoint.api.domain.payment.dto.PaymentStatusResponse;


import com.copypoint.api.domain.payment.service.PaymentService;
import com.copypoint.api.domain.paymentattempt.entity.PaymentAttemptStatus;
import com.copypoint.api.domain.paymentattempt.service.PaymentAttemptService;
import com.copypoint.api.infra.mercadopago.handler.MercadoPagoWebhookHandler;
import com.copypoint.api.infra.mercadopago.manager.PaymentStatusManager;
import com.copypoint.api.infra.mercadopago.orchestrator.PaymentCreationOrchestrator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mercadopago.client.merchantorder.MerchantOrderClient;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.merchantorder.MerchantOrder;
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
    private PaymentCreationOrchestrator paymentCreationOrchestrator;

    @Autowired
    private PaymentStatusManager paymentStatusManager;

    @Autowired
    private MercadoPagoWebhookHandler webhookHandler;

    @Autowired
    private PaymentQueryService paymentQueryService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentAttemptService paymentAttemptService;

    public MercadoPagoService() {
        // Configure ObjectMapper with Java 8 time support
        ObjectMapper objectMapper = new ObjectMapper();
        // Register the JavaTimeModule to handle Java 8 date/time types
        objectMapper.registerModule(new JavaTimeModule());
        // Disable writing dates as timestamps
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Crea un pago delegando al orquestador especializado
     */
    public PaymentGatewayResponse createPayment(PaymentRequest request) throws MPException, MPApiException {
        return paymentCreationOrchestrator.createPayment(request);
    }

    /**
     * Consulta el estado de un pago delegando al gestor de estados
     */
    public PaymentStatusResponse getPaymentStatus(Long paymentId) {
        return paymentStatusManager.getPaymentStatus(paymentId);
    }

    /**
     * Maneja el webhook de MercadoPago de manera agnóstica
     * @param dataId ID que llega en el webhook (puede ser payment ID, order ID, etc.)
     * @param topic Tipo de notificación (payment, merchant_order, etc.)
     * @param payload Payload completo del webhook
     * @return true si se procesó correctamente
     */
    public boolean handleWebhook(String dataId, String topic, String payload) {
        try {
            logger.info("Procesando webhook MercadoPago - dataId: {}, topic: {}", dataId, topic);

            // Buscar el payment por cualquier ID
            Optional<Payment> paymentOpt = paymentService.findByAnyGatewayId(dataId);

            if (paymentOpt.isEmpty()) {
                logger.warn("No se encontró payment para dataId: {}", dataId);
                return false;
            }

            Payment payment = paymentOpt.get();

            // Procesar según el tipo de webhook
            return switch (topic.toLowerCase()) {
                case "payment" -> handlePaymentWebhookUpdate(payment, dataId, payload);
                case "merchant_order" -> handleMerchantOrderWebhookUpdate(payment, dataId, payload);
                default -> {
                    logger.warn("Tipo de webhook no manejado: {}", topic);
                    yield handleGenericWebhookUpdate(payment, dataId, payload);
                }
            };

        } catch (Exception e) {
            logger.error("Error procesando webhook MercadoPago: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Consulta un pago por gateway ID delegando al servicio de consultas
     * @deprecated Usar PaymentService.findByAnyGatewayId directamente
     */
    @Deprecated
    public Payment getPaymentByGatewayId(String paymentId) {
        return paymentQueryService.getPaymentByGatewayId(paymentId);
    }

    /**
     * Consulta un pago por ID delegando al servicio de consultas
     */
    public Payment getPaymentById(Long paymentId) {
        return paymentQueryService.getPaymentById(paymentId);
    }

    /**
     * Actualiza el estado de un pago desde el gateway
     * Método de conveniencia que delega al gestor de estados
     */
    public void updatePaymentStatusFromGateway(Payment payment) {
        paymentStatusManager.updatePaymentStatusFromGateway(payment);
    }

    /**
     * Procesa webhook de payment
     */
    private boolean handlePaymentWebhookUpdate(Payment payment, String mpPaymentId, String payload) {
        try {
            // Consultar el estado actual del payment en MercadoPago
            PaymentClient client = new PaymentClient();
            com.mercadopago.resources.payment.Payment mpPayment = client.get(Long.parseLong(mpPaymentId));

            // Mapear el estado de MercadoPago a nuestro enum
            PaymentStatus newStatus = mapMercadoPagoStatus(mpPayment.getStatus());

            // Actualizar el payment si el estado cambió
            if (newStatus != payment.getStatus()) {
                paymentService.updatePaymentStatus(payment.getId(), newStatus);
                logger.info("Payment {} actualizado - Estado: {} -> {}",
                        payment.getId(), payment.getStatus(), newStatus);
            }

            // Asegurar que tenemos el gatewayPaymentId
            if (!payment.hasGatewayPaymentId()) {
                paymentService.updatePaymentGatewayPaymentId(payment.getId(), mpPaymentId);
            }

            // Actualizar gatewayTransactionId si está disponible
            if (mpPayment.getTransactionDetails() != null &&
                    mpPayment.getTransactionDetails().getFinancialInstitution() != null) {

                String transactionId = mpPayment.getTransactionDetails().getFinancialInstitution();
                if (!payment.hasGatewayTransactionId() && transactionId != null) {
                    paymentService.updatePaymentGatewayTransactionId(payment.getId(), transactionId);
                }
            }

            // Crear registro del intento de webhook
            paymentAttemptService.createPaymentAttempt(
                    payment,
                    mapStatusToAttemptStatus(newStatus),
                    payload
            );

            return true;

        } catch (Exception e) {
            logger.error("Error procesando webhook de payment {}: {}", mpPaymentId, e.getMessage());
            return false;
        }
    }

    /**
     * Procesa webhook de merchant order
     */
    private boolean handleMerchantOrderWebhookUpdate(Payment payment, String mpOrderId, String payload) {
        try {
            // Consultar la merchant order en MercadoPago
            MerchantOrderClient client = new MerchantOrderClient();
            MerchantOrder merchantOrder = client.get(Long.parseLong(mpOrderId));

            // Las merchant orders pueden tener múltiples payments
            // Por ahora, solo registramos la notificación
            logger.info("Merchant Order procesada - ID: {}, Status: {}, Total: {}",
                    merchantOrder.getId(), merchantOrder.getOrderStatus(), merchantOrder.getTotalAmount());

            // Crear registro del intento de webhook
            paymentAttemptService.createPaymentAttempt(
                    payment,
                    PaymentAttemptStatus.PENDING,
                    payload
            );

            return true;

        } catch (Exception e) {
            logger.error("Error procesando webhook de merchant order {}: {}", mpOrderId, e.getMessage());
            return false;
        }
    }

    /**
     * Procesa webhooks genéricos
     */
    private boolean handleGenericWebhookUpdate(Payment payment, String dataId, String payload) {
        try {
            logger.info("Procesando webhook genérico para payment: {}, dataId: {}",
                    payment.getId(), dataId);

            // Solo registrar la notificación
            paymentAttemptService.createPaymentAttempt(
                    payment,
                    PaymentAttemptStatus.PENDING,
                    payload
            );

            return true;

        } catch (Exception e) {
            logger.error("Error procesando webhook genérico: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Mapea estados de MercadoPago a nuestros PaymentAttemptStatus
     */
    private PaymentAttemptStatus mapStatusToAttemptStatus(PaymentStatus paymentStatus) {
        return switch (paymentStatus) {
            case APPROVED -> PaymentAttemptStatus.SUCCEEDED;
            case REJECTED, CANCELLED -> PaymentAttemptStatus.FAILED;
            case REFUNDED -> PaymentAttemptStatus.REFUNDED;
            default -> PaymentAttemptStatus.PENDING;
        };
    }

    /**
     * Método legacy para compatibilidad
     * @deprecated Usar handleWebhook(String, String, String) en su lugar
     */
    @Deprecated
    public void handleWebhook(String paymentId, String status) {
        handleWebhook(paymentId, "payment", "{\"legacy_status\":\"" + status + "\"}");
    }


    /**
     * Mapea los estados de MercadoPago a nuestro enum PaymentStatus
     * Estados de MercadoPago:
     * - pending: El pago está pendiente de procesamiento
     * - approved: El pago fue aprobado y acreditado
     * - authorized: El pago fue autorizado pero no capturado
     * - in_process: El pago está en proceso de revisión
     * - in_mediation: El pago está en mediación o disputa
     * - rejected: El pago fue rechazado
     * - cancelled: El pago fue cancelado
     * - refunded: El pago fue reembolsado
     * - charged_back: El pago tuvo un contracargo
     *
     * @param mercadoPagoStatus Estado del pago en MercadoPago
     * @return PaymentStatus correspondiente en nuestro sistema
     */
    private PaymentStatus mapMercadoPagoStatus(String mercadoPagoStatus) {
        if (mercadoPagoStatus == null || mercadoPagoStatus.trim().isEmpty()) {
            logger.warn("Estado de MercadoPago nulo o vacío, retornando PENDING");
            return PaymentStatus.PENDING;
        }

        return switch (mercadoPagoStatus.toLowerCase().trim()) {
            // Estados exitosos
            case "approved" -> PaymentStatus.APPROVED;
            case "authorized" -> PaymentStatus.AUTHORIZED;

            // Estados de procesamiento
            case "pending" -> PaymentStatus.PENDING;
            case "in_process" -> PaymentStatus.PROCESSING;

            // Estados fallidos
            case "rejected" -> PaymentStatus.REJECTED;
            case "cancelled" -> PaymentStatus.CANCELLED;

            // Estados de reembolso
            case "refunded" -> PaymentStatus.REFUNDED;
            case "partially_refunded" -> PaymentStatus.PARTIALLY_REFUNDED;

            // Estados de disputa
            case "in_mediation", "charged_back" -> PaymentStatus.DISPUTED;

            // Estados adicionales que podrían aparecer
            case "expired" -> PaymentStatus.EXPIRED;
            case "voided" -> PaymentStatus.VOIDED;
            case "on_hold" -> PaymentStatus.ON_HOLD;

            // Estado por defecto para casos no contemplados
            default -> {
                logger.warn("Estado de MercadoPago desconocido: '{}', mapeando a PENDING", mercadoPagoStatus);
                yield PaymentStatus.PENDING;
            }
        };
    }

}