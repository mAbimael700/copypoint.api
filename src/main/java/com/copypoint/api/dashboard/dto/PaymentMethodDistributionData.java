package com.copypoint.api.dashboard.dto;

public record PaymentMethodDistributionData(
        String methodDescription,
        Integer usageCount,
        Double percentage
) {}
