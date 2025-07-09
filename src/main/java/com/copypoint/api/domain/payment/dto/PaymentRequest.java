package com.copypoint.api.domain.payment.dto;

import jakarta.validation.constraints.NotNull;

public record PaymentRequest(
        @NotNull
        Long saleId,

        @NotNull
        PayerInfo payer,

        String description,
        Double amount,
        String currency
) {
}
