package com.copypoint.api.domain.payment.dto;

import com.copypoint.api.domain.payment.Payment;
import com.copypoint.api.domain.payment.PaymentStatus;

import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        String paymentMethod,
        Double amount,
        PaymentStatus status,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt

) {

    public PaymentResponse(Payment payment) {
        this(
                payment.getId(),
                payment.getPaymentMethod().getDescription(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getCreatedAt(),
                payment.getModifiedAt()
        );
    }
}
