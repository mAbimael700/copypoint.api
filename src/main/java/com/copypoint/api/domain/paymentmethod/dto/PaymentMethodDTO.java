package com.copypoint.api.domain.paymentmethod.dto;

import com.copypoint.api.domain.paymentmethod.PaymentMethod;

public record PaymentMethodDTO(
        Long id,
        String description
) {
    public PaymentMethodDTO(PaymentMethod paymentMethod) {
        this(
                paymentMethod.getId(),
                paymentMethod.getDescription()
        );
    }
}
