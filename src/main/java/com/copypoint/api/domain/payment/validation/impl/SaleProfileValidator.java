package com.copypoint.api.domain.payment.validation.impl;

import com.copypoint.api.domain.payment.dto.PaymentRequest;
import com.copypoint.api.domain.payment.validation.PaymentValidator;
import com.copypoint.api.domain.payment.validation.ValidationResult;
import com.copypoint.api.domain.sale.Sale;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SaleProfileValidator implements PaymentValidator {
    @Override
    public ValidationResult validatePayment(Sale sale, PaymentRequest paymentRequest) {
        List<String> errors = new ArrayList<>();

        if (sale.getSaleProfiles() == null || sale.getSaleProfiles().isEmpty()) {
            errors.add("La venta debe tener al menos un perfil de venta asociado");
            return ValidationResult.failure(errors);
        }

        return ValidationResult.success();
    }

    @Override
    public String getValidatorType() {
        return "SALE_PROFILE";
    }
}
