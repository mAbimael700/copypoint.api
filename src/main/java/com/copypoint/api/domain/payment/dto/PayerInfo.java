package com.copypoint.api.domain.payment.dto;

public record PayerInfo(
        String firstName,
        String lastName,
        String email,
        String phone,
        String identification,
        String identificationType
) {
}
