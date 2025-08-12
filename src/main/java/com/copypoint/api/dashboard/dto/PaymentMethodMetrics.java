package com.copypoint.api.dashboard.dto;

import java.util.List;

public record PaymentMethodMetrics(
        String mostUsedMethod,
        List<GatewayRevenueData> revenueByGateway
) {}
