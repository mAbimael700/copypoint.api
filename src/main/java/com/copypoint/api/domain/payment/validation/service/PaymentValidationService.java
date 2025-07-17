package com.copypoint.api.domain.payment.validation.service;


import com.copypoint.api.domain.payment.dto.PaymentRequest;
import com.copypoint.api.domain.payment.validation.PaymentValidator;
import com.copypoint.api.domain.payment.validation.ValidationResult;
import com.copypoint.api.domain.sale.Sale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PaymentValidationService {

    @Autowired
    private List<PaymentValidator> validators;

    /**
     * Ejecuta todas las validaciones registradas
     */
    public ValidationResult validatePayment(Sale sale, PaymentRequest paymentRequest) {
        List<String> allErrors = new ArrayList<>();
        List<String> allWarnings = new ArrayList<>();

        for (PaymentValidator validator : validators) {
            ValidationResult result = validator.validatePayment(sale, paymentRequest);

            if (!result.isValid()) {
                allErrors.addAll(result.getErrors());
            }

            allWarnings.addAll(result.getWarnings());
        }

        if (!allErrors.isEmpty()) {
            return ValidationResult.failure(allErrors);
        }

        return allWarnings.isEmpty() ?
                ValidationResult.success() :
                ValidationResult.success(allWarnings);
    }

    /**
     * Ejecuta validaciones específicas por tipo
     */
    public ValidationResult validatePayment(Sale sale, PaymentRequest paymentRequest, List<String> validatorTypes) {
        List<String> allErrors = new ArrayList<>();
        List<String> allWarnings = new ArrayList<>();

        for (PaymentValidator validator : validators) {
            if (validatorTypes.contains(validator.getValidatorType())) {
                ValidationResult result = validator.validatePayment(sale, paymentRequest);

                if (!result.isValid()) {
                    allErrors.addAll(result.getErrors());
                }

                allWarnings.addAll(result.getWarnings());
            }
        }

        if (!allErrors.isEmpty()) {
            return ValidationResult.failure(allErrors);
        }

        return allWarnings.isEmpty() ?
                ValidationResult.success() :
                ValidationResult.success(allWarnings);
    }

    /**
     * Obtiene todos los validadores disponibles
     */
    public List<String> getAvailableValidators() {
        return validators.stream()
                .map(PaymentValidator::getValidatorType)
                .toList();
    }
}
