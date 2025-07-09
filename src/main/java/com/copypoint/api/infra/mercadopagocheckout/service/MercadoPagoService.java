package com.copypoint.api.infra.mercadopagocheckout.service;

import com.copypoint.api.domain.payment.Payment;
import com.copypoint.api.domain.payment.PaymentStatus;
import com.copypoint.api.domain.payment.dto.PaymentRequest;
import com.copypoint.api.domain.payment.dto.PaymentResponse;
import com.copypoint.api.domain.payment.dto.PaymentStatusResponse;

import com.copypoint.api.domain.payment.service.PaymentService;

import com.copypoint.api.domain.paymentattempt.PaymentAttemptStatus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class MercadoPagoService {
    @Autowired
    private PaymentService paymentService;

    @Autowired
    private MercadoPagoGatewayService mercadoPagoGatewayService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public PaymentResponse createPayment(PaymentRequest request) {
        try {
            // Crear el pago en la base de datos usando el servicio de dominio
            Payment payment = paymentService.createPayment(
                    request.saleId(),
                    request.amount(),
                    request.currency()
            );

            // Crear la preferencia en MercadoPago
            Preference preference = mercadoPagoGatewayService.createPreference(payment, request);

            // Actualizar el payment con el ID de MercadoPago
            payment = paymentService.updatePaymentGatewayId(payment.getId(), preference.getId());

            // Crear registro de intento de pago
            paymentService.createPaymentAttempt(
                    payment,
                    PaymentAttemptStatus.PENDING,
                    objectMapper.writeValueAsString(preference)
            );

            return new PaymentResponse(
                    true,
                    "Pago creado exitosamente",
                    preference.getInitPoint(),
                    preference.getId(),
                    payment.getId().toString(),
                    PaymentStatus.PENDING
            );

        } catch (IllegalArgumentException e) {
            return new PaymentResponse(false, e.getMessage(), null, null, null, null);
        } catch (MPException | MPApiException e) {
            return new PaymentResponse(false, "Error al crear el pago: " + e.getMessage(), null, null, null, null);
        } catch (Exception e) {
            return new PaymentResponse(false, "Error interno: " + e.getMessage(), null, null, null, null);
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

    public void handleWebhook(String paymentId, String status) {
        try {
            PaymentStatus newStatus = mercadoPagoGatewayService.mapMercadoPagoStatus(status);
            Payment payment = paymentService.updatePaymentStatusByGatewayId(paymentId, newStatus);

            // Crear registro de intento
            paymentService.createPaymentAttempt(
                    payment,
                    PaymentAttemptStatus.valueOf(newStatus.name()),
                    "{\"status\":\"" + status + "\"}"
            );

        } catch (Exception e) {
            System.err.println("Error procesando webhook: " + e.getMessage());
        }
    }

    private void updatePaymentStatusFromGateway(Payment payment) {
        try {
            PaymentStatus gatewayStatus = mercadoPagoGatewayService.getPaymentStatusFromGateway(payment.getGatewayId());

            if (gatewayStatus != null && gatewayStatus != payment.getStatus()) {
                paymentService.updatePaymentStatus(payment.getId(), gatewayStatus);
            }

        } catch (Exception e) {
            // Log del error pero no fallar
            System.err.println("Error al actualizar estado desde MercadoPago: " + e.getMessage());
        }
    }
}
