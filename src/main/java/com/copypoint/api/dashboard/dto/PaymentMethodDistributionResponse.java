package com.copypoint.api.dashboard.dto;

import java.util.List;

public record PaymentMethodDistributionResponse(
        List<PaymentMethodDistributionData> distribution
) {}
