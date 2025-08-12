package com.copypoint.api.dashboard.dto;

public record PaymentStatusMetrics(
        Double successRate,
        Integer pendingPayments,
        Integer failedPayments,
        Integer totalPayments
) {}
