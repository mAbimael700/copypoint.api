package com.copypoint.api.domain.payment.validation;

import com.copypoint.api.domain.payment.dto.PaymentRequest;
import com.copypoint.api.domain.sale.Sale;

public interface PaymentValidator {

    /**
     * Valida si se puede crear un pago para una venta específica
     * @param sale La venta para la cual se quiere crear el pago
     * @param paymentRequest La request con los datos del pago
     * @return ValidationResult con el resultado de la validación
     */
    ValidationResult validatePayment(Sale sale, PaymentRequest paymentRequest);

    /**
     * Obtiene el tipo de validador
     * @return String identificando el tipo de validador
     */
    String getValidatorType();
}
