package com.copypoint.api.domain.payment.dto;

import jakarta.validation.constraints.NotNull;

public record PaymentRequest(
        @NotNull
        Long saleId,

        @NotNull
        PayerInfo payer,
        @NotNull
        String description,
        @NotNull
        Double amount,
        @NotNull
        String currency
) {
}
