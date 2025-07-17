package com.copypoint.api.domain.payment.dto;

import com.copypoint.api.domain.payment.PaymentStatus;

public record PaymentGatewayResponse(
        Boolean success,
        String message,
        String checkoutUrl,
        String preferenceId,
        String paymentId,
        PaymentStatus status
) {
}
