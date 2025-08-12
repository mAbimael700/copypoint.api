package com.copypoint.api.dashboard.dto;

public record PaymentAttemptData(
        String status,
        Integer count
) {}
