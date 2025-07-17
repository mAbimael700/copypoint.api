package com.copypoint.api.infra.mercadopago.service;

import com.copypoint.api.domain.payment.Payment;
import com.copypoint.api.domain.payment.dto.PaymentRequest;
import com.copypoint.api.domain.payment.dto.PaymentGatewayResponse;
import com.copypoint.api.domain.payment.dto.PaymentStatusResponse;


import com.copypoint.api.infra.mercadopago.handler.MercadoPagoWebhookHandler;
import com.copypoint.api.infra.mercadopago.manager.PaymentStatusManager;
import com.copypoint.api.infra.mercadopago.orchestrator.PaymentCreationOrchestrator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


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
     * Maneja webhooks delegando al manejador especializado
     */
    public void handleWebhook(String webHookPaymentId, String webHookStatus) {
        webhookHandler.handleWebhook(webHookPaymentId, webHookStatus);
    }

    /**
     * Consulta un pago por gateway ID delegando al servicio de consultas
     */
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
}