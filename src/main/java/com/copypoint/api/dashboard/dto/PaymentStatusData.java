package com.copypoint.api.dashboard.dto;

public record PaymentStatusData(
        String status,
        Integer count,
        Double percentage
) {}
