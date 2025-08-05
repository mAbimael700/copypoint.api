package com.copypoint.api.domain.payment.service;

import com.copypoint.api.domain.payment.entity.Payment;
import com.copypoint.api.domain.payment.entity.PaymentStatus;
import com.copypoint.api.domain.payment.dto.PaymentRequest;
import com.copypoint.api.domain.payment.dto.PaymentStatusResponse;
import com.copypoint.api.domain.payment.repository.PaymentRepository;
import com.copypoint.api.domain.payment.validation.ValidationResult;
import com.copypoint.api.domain.payment.validation.service.PaymentValidationService;
import com.copypoint.api.domain.paymentattempt.entity.PaymentAttempt;
import com.copypoint.api.domain.paymentattempt.entity.PaymentAttemptStatus;
import com.copypoint.api.domain.paymentattempt.repository.PaymentAttemptRepository;
import com.copypoint.api.domain.paymentmethod.PaymentMethod;
import com.copypoint.api.domain.paymentmethod.repository.PaymentMethodRepository;
import com.copypoint.api.domain.sale.Sale;
import com.copypoint.api.domain.sale.repository.SaleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Payment createPayment(PaymentRequest paymentRequest, String paymentMethod) {
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

        Optional<PaymentMethod> paymentMethodOpt = paymentMethodRepository.findByDescription(paymentMethod);

        if (paymentMethodOpt.isEmpty()) {
            throw new IllegalArgumentException("Validación fallida: Payment method no encontrado");
        }

        // Crear el pago en la base de datos
        Payment payment = new Payment();
        payment.setSale(sale);
        payment.setAmount(paymentRequest.amount());
        payment.setCurrency(paymentRequest.currency() != null ? paymentRequest.currency() : sale.getCurrency());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentMethod(paymentMethodOpt.get());
        payment.setCreatedAt(LocalDateTime.now());

        return paymentRepository.save(payment);
    }

    /**
     * Método legacy actualizado para compatibilidad
     *
     * @deprecated Usar updatePaymentGatewayIntentId o updatePaymentGatewayPaymentId
     */
    @Deprecated
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

    public Page<Payment> getPayments(Pageable pageable) {
        return paymentRepository.findAll(pageable);
    }

    public Page<Payment> getPaymentsByCopypoint(Long copypointId, Pageable pageable) {
        return paymentRepository.findBySale_CopypointId(copypointId, pageable);
    }

    public Page<Payment> getPaymentsBySale(Long saleId, Pageable pageable) {
        return paymentRepository.findBySaleId(saleId, pageable);
    }


    /**
     * Actualiza el payment con el Intent ID de la pasarela (checkout creado)
     */
    public Payment updatePaymentGatewayIntentId(Long paymentId, String gatewayIntentId) {
        Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
        if (paymentOpt.isEmpty()) {
            throw new IllegalArgumentException("Pago no encontrado");
        }

        Payment payment = paymentOpt.get();
        payment.setGatewayIntentId(gatewayIntentId);
        payment.setModifiedAt(LocalDateTime.now());

        return paymentRepository.save(payment);
    }

    /**
     * Actualiza el payment con el Payment ID de la pasarela (pago completado)
     */
    public Payment updatePaymentGatewayPaymentId(Long paymentId, String gatewayPaymentId) {
        Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
        if (paymentOpt.isEmpty()) {
            throw new IllegalArgumentException("Pago no encontrado");
        }

        Payment payment = paymentOpt.get();
        payment.setGatewayPaymentId(gatewayPaymentId);
        payment.setModifiedAt(LocalDateTime.now());

        return paymentRepository.save(payment);
    }

    /**
     * Actualiza el payment con el Transaction ID de la pasarela (ID bancario)
     */
    public Payment updatePaymentGatewayTransactionId(Long paymentId, String gatewayTransactionId) {
        Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
        if (paymentOpt.isEmpty()) {
            throw new IllegalArgumentException("Pago no encontrado");
        }

        Payment payment = paymentOpt.get();
        payment.setGatewayTransactionId(gatewayTransactionId);
        payment.setModifiedAt(LocalDateTime.now());

        return paymentRepository.save(payment);
    }

    /**
     * Busca payment por Intent ID (agnóstico)
     */
    public Optional<Payment> findByGatewayIntentId(String gatewayIntentId) {
        return paymentRepository.findByGatewayIntentId(gatewayIntentId);
    }

    /**
     * Busca payment por Payment ID (agnóstico)
     */
    public Optional<Payment> findByGatewayPaymentId(String gatewayPaymentId) {
        return paymentRepository.findByGatewayPaymentId(gatewayPaymentId);
    }

    /**
     * Busca payment por Transaction ID (agnóstico)
     */
    public Optional<Payment> findByGatewayTransactionId(String gatewayTransactionId) {
        return paymentRepository.findByGatewayTransactionId(gatewayTransactionId);
    }

    /**
     * Busca payment por cualquier ID de pasarela (agnóstico)
     * Útil para webhooks
     */
    public Optional<Payment> findByAnyGatewayId(String gatewayId) {
        return paymentRepository.findByAnyGatewayId(gatewayId);
    }

    /**
     * Obtiene payments por pasarela específica
     */
    public Page<Payment> getPaymentsByGateway(String gateway, Pageable pageable) {
        return paymentRepository.findByGateway(gateway, pageable);
    }
}
