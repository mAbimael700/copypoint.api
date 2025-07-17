package com.copypoint.api.domain.payment.validation.impl;

import com.copypoint.api.domain.payment.dto.PaymentRequest;
import com.copypoint.api.domain.payment.validation.PaymentValidator;
import com.copypoint.api.domain.payment.validation.ValidationResult;
import com.copypoint.api.domain.sale.Sale;
import com.copypoint.api.domain.sale.SaleStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class SaleStatusValidator implements PaymentValidator {
    private static final Set<SaleStatus> VALID_STATUSES = Set.of(
            SaleStatus.PAYMENT_PENDING,
            SaleStatus.PARTIALLY_PAID,
            SaleStatus.PENDING
    );

    @Override
    public ValidationResult validatePayment(Sale sale, PaymentRequest paymentRequest) {
        List<String> errors = new ArrayList<>();

        if (sale == null) {
            errors.add("La venta no puede ser nula");
            return ValidationResult.failure(errors);
        }

        if (sale.getStatus() == null) {
            errors.add("El estado de la venta no puede ser nulo");
            return ValidationResult.failure(errors);
        }

        if (!VALID_STATUSES.contains(sale.getStatus())) {
            errors.add(String.format(
                    "El estado de la venta debe ser %s o %s. Estado actual: %s",
                    SaleStatus.PAYMENT_PENDING,
                    SaleStatus.PARTIALLY_PAID,
                    sale.getStatus()
            ));
            return ValidationResult.failure(errors);
        }

        return ValidationResult.success();
    }

    @Override
    public String getValidatorType() {
        return "SALE_STATUS";
    }
}
