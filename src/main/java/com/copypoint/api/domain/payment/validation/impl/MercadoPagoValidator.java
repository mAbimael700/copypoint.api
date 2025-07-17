package com.copypoint.api.domain.payment.validation.impl;

import com.copypoint.api.domain.mercadopagoconfiguration.service.MercadoPagoConfigurationService;
import com.copypoint.api.domain.payment.dto.PaymentRequest;
import com.copypoint.api.domain.payment.validation.PaymentValidator;
import com.copypoint.api.domain.payment.validation.ValidationResult;
import com.copypoint.api.domain.sale.Sale;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class MercadoPagoValidator implements PaymentValidator {
    @Autowired
    private MercadoPagoConfigurationService mercadoPagoConfigurationService;

    @Override
    public ValidationResult validatePayment(Sale sale, PaymentRequest paymentRequest) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Validar que existe configuración de MercadoPago para la venta
        try {
            String vendorEmail = mercadoPagoConfigurationService.getVendorEmailForSale(sale);
            if (vendorEmail == null || vendorEmail.trim().isEmpty()) {
                errors.add("No se encontró configuración de MercadoPago para esta venta");
                return ValidationResult.failure(errors);
            }
        } catch (Exception e) {
            errors.add("Error al validar configuración de MercadoPago: " + e.getMessage());
            return ValidationResult.failure(errors);
        }

        // Validaciones específicas de MercadoPago
        if (paymentRequest.amount() != null && paymentRequest.amount() < 0.50) {
            errors.add("MercadoPago requiere un monto mínimo de $0.50");
        }

        if (paymentRequest.amount() != null && paymentRequest.amount() > 10000000) {
            warnings.add("Monto elevado, puede requerir validación adicional en MercadoPago");
        }

        if (!errors.isEmpty()) {
            return ValidationResult.failure(errors);
        }

        return warnings.isEmpty() ? ValidationResult.success() : ValidationResult.success(warnings);
    }

    @Override
    public String getValidatorType() {
        return "MERCADOPAGO";
    }
}
