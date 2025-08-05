package com.copypoint.api.domain.payment.validation.impl;

import com.copypoint.api.domain.payment.entity.Payment;
import com.copypoint.api.domain.payment.entity.PaymentStatus;
import com.copypoint.api.domain.payment.dto.PaymentRequest;
import com.copypoint.api.domain.payment.validation.PaymentValidator;
import com.copypoint.api.domain.payment.validation.ValidationResult;
import com.copypoint.api.domain.sale.Sale;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class PaymentAmountValidator implements PaymentValidator {
    private static final Set<PaymentStatus> COMPLETED_STATUSES = Set.of(
            PaymentStatus.COMPLETED,
            PaymentStatus.APPROVED,
            PaymentStatus.CAPTURED
    );

    private static final Set<PaymentStatus> PENDING_STATUSES = Set.of(
            PaymentStatus.PENDING,
            PaymentStatus.PROCESSING,
            PaymentStatus.AUTHORIZED
    );

    @Override
    public ValidationResult validatePayment(Sale sale, PaymentRequest paymentRequest) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        if (paymentRequest == null || paymentRequest.amount() == null) {
            errors.add("El monto del pago no puede ser nulo");
            return ValidationResult.failure(errors);
        }

        if (paymentRequest.amount() <= 0) {
            errors.add("El monto del pago debe ser mayor a cero");
            return ValidationResult.failure(errors);
        }

        if (sale.getTotal() == null) {
            errors.add("El total de la venta no puede ser nulo");
            return ValidationResult.failure(errors);
        }

        // Calcular pagos completados y pendientes
        double completedPayments = calculateCompletedPayments(sale);
        double pendingPayments = calculatePendingPayments(sale);
        double totalPaid = completedPayments + pendingPayments;

        // Validar que el nuevo pago no exceda el total de la venta
        double newTotal = totalPaid + paymentRequest.amount();
        if (newTotal > sale.getTotal()) {
            errors.add(String.format(
                    "El monto del pago (%.2f) excede el saldo pendiente. " +
                            "Total venta: %.2f, Ya pagado/pendiente: %.2f, Saldo disponible: %.2f",
                    paymentRequest.amount(),
                    sale.getTotal(),
                    totalPaid,
                    sale.getTotal() - totalPaid
            ));
            return ValidationResult.failure(errors);
        }

        // Advertencia si hay pagos pendientes
        if (pendingPayments > 0) {
            warnings.add(String.format(
                    "Existen pagos pendientes por %.2f. Total con este pago: %.2f",
                    pendingPayments,
                    newTotal
            ));
        }

        return warnings.isEmpty() ? ValidationResult.success() : ValidationResult.success(warnings);
    }

    private double calculateCompletedPayments(Sale sale) {
        return sale.getPayments().stream()
                .filter(payment -> COMPLETED_STATUSES.contains(payment.getStatus()))
                .mapToDouble(Payment::getAmount)
                .sum();
    }

    private double calculatePendingPayments(Sale sale) {
        return sale.getPayments().stream()
                .filter(payment -> PENDING_STATUSES.contains(payment.getStatus()))
                .mapToDouble(Payment::getAmount)
                .sum();
    }

    @Override
    public String getValidatorType() {
        return "PAYMENT_AMOUNT";
    }
}
