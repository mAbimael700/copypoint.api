package com.copypoint.api.domain.payment.dto;

import com.copypoint.api.domain.payment.PaymentStatus;

public record PaymentStatusResponse(
        String paymentId,
        PaymentStatus status,
        String gatewayResponse,
        Double amount,
        String currency,
        String errorMessage
) {
}
