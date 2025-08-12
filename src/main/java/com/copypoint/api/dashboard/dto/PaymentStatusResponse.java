package com.copypoint.api.dashboard.dto;

import java.util.List;

// DTOs para Dashboard de Estados de Pagos
public record PaymentStatusResponse(
        List<PaymentStatusData> statusDistribution,
        PaymentStatusMetrics metrics
) {}
