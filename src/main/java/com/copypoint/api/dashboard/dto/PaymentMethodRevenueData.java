package com.copypoint.api.dashboard.dto;

public record PaymentMethodRevenueData(
        String methodDescription,
        String gateway,
        Double totalRevenue,
        Integer transactionCount
) {}
