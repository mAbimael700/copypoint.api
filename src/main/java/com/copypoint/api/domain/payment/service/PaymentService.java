package com.copypoint.api.domain.payment.service;

import com.copypoint.api.domain.payment.Payment;
import com.copypoint.api.domain.payment.PaymentStatus;
import com.copypoint.api.domain.payment.dto.PaymentRequest;
import com.copypoint.api.domain.payment.dto.PaymentStatusResponse;
import com.copypoint.api.domain.payment.repository.PaymentRepository;
import com.copypoint.api.domain.payment.validation.ValidationResult;
import com.copypoint.api.domain.payment.validation.service.PaymentValidationService;
import com.copypoint.api.domain.paymentattempt.PaymentAttempt;
import com.copypoint.api.domain.paymentattempt.PaymentAttemptStatus;
import com.copypoint.api.domain.paymentattempt.repository.PaymentAttemptRepository;
import com.copypoint.api.domain.sale.Sale;
import com.copypoint.api.domain.sale.repository.SaleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class PaymentService {
    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentAttemptRepository paymentAttemptRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private PaymentValidationService paymentValidationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Payment createPayment(PaymentRequest paymentRequest) {
        // Verificar que la venta existe
        Optional<Sale> saleOpt = saleRepository.findById(paymentRequest.saleId());
        if (saleOpt.isEmpty()) {
            throw new IllegalArgumentException("Venta no encontrada");
        }

        Sale sale = saleOpt.get();

        // Ejecutar validaciones
        ValidationResult validationResult = paymentValidationService.validatePayment(sale, paymentRequest);

        if (!validationResult.isValid()) {
            throw new IllegalArgumentException("Validación fallida: " + String.join(", ", validationResult.getErrors()));
        }

        // Crear el pago en la base de datos
        Payment payment = new Payment();
        payment.setSale(sale);
        payment.setAmount(paymentRequest.amount());
        payment.setCurrency(paymentRequest.currency() != null ? paymentRequest.currency() : sale.getCurrency());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());

        return paymentRepository.save(payment);
    }

    public Payment updatePaymentGatewayId(Long paymentId, String gatewayId) {
        Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
        if (paymentOpt.isEmpty()) {
            throw new IllegalArgumentException("Pago no encontrado");
        }

        Payment payment = paymentOpt.get();
        payment.setGatewayId(gatewayId);
        payment.setModifiedAt(LocalDateTime.now());

        return paymentRepository.save(payment);
    }

    public Payment updatePaymentStatus(Long paymentId, PaymentStatus status) {
        Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
        if (paymentOpt.isEmpty()) {
            throw new IllegalArgumentException("Pago no encontrado");
        }

        Payment payment = paymentOpt.get();
        payment.setStatus(status);
        payment.setModifiedAt(LocalDateTime.now());

        return paymentRepository.save(payment);
    }

    public Payment updatePaymentStatusByGatewayId(String gatewayId, PaymentStatus status) {
        Optional<Payment> paymentOpt = paymentRepository.findByGatewayId(gatewayId);
        if (paymentOpt.isEmpty()) {
            throw new IllegalArgumentException("Pago no encontrado con gatewayId: " + gatewayId);
        }

        Payment payment = paymentOpt.get();

        // Solo actualizar si el estado es diferente
        if (status != payment.getStatus()) {
            payment.setStatus(status);
            payment.setModifiedAt(LocalDateTime.now());
            return paymentRepository.save(payment);
        }

        return payment;
    }

    public PaymentAttempt createPaymentAttempt(Payment payment, PaymentAttemptStatus status, String gatewayResponse) {
        PaymentAttempt attempt = new PaymentAttempt();
        attempt.setPaymentReference(payment);
        attempt.setStatus(status);
        attempt.setGatewayResponse(gatewayResponse);
        attempt.setCreatedAt(LocalDateTime.now());

        return paymentAttemptRepository.save(attempt);
    }

    public PaymentStatusResponse getPaymentStatus(Long paymentId) {
        Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
        if (paymentOpt.isEmpty()) {
            return new PaymentStatusResponse(null, null, null, null, null, "Pago no encontrado");
        }

        Payment payment = paymentOpt.get();

        return new PaymentStatusResponse(
                payment.getId().toString(),
                payment.getStatus(),
                null,
                payment.getAmount(),
                payment.getCurrency(),
                null
        );
    }

    public Optional<Payment> findById(Long paymentId) {
        return paymentRepository.findById(paymentId);
    }

    public Optional<Payment> findByGatewayId(String gatewayId) {
        return paymentRepository.findByGatewayId(gatewayId);
    }

    /**
     * Valida si se puede crear un pago sin crearlo
     */
    public ValidationResult validatePaymentCreation(PaymentRequest paymentRequest) {
        Optional<Sale> saleOpt = saleRepository.findById(paymentRequest.saleId());
        if (saleOpt.isEmpty()) {
            return ValidationResult.failure("Venta no encontrada");
        }

        return paymentValidationService.validatePayment(saleOpt.get(), paymentRequest);
    }
}
