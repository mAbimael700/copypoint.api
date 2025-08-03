package com.copypoint.api.domain.payment.dto;

import com.copypoint.api.domain.payment.Payment;
import com.copypoint.api.domain.payment.PaymentStatus;
import com.copypoint.api.domain.sale.dto.SaleDTO;

import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        String paymentMethod,
        Double amount,
        SaleDTO sale,
        PaymentStatus status,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        String transactionId

) {

    public PaymentResponse(Payment payment) {

        this(
                payment.getId(),
                payment.getPaymentMethod().getDescription(),
                payment.getAmount(),
                new SaleDTO(payment.getSale()),
                payment.getStatus(),
                payment.getCreatedAt(),
                payment.getModifiedAt(),
                payment.getGatewayId()
        );
    }
}
