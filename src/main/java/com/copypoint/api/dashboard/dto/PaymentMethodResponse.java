package com.copypoint.api.dashboard.dto;

import java.util.List;

// DTOs para Dashboard de Métodos de Pago
public record PaymentMethodResponse(
        List<PaymentMethodRevenueData> revenueByMethod,
        PaymentMethodMetrics metrics
) {}
