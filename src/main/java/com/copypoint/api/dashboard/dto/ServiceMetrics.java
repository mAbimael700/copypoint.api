package com.copypoint.api.dashboard.dto;

import java.util.List;

public record ServiceMetrics(
        List<ServiceSalesData> top5Services,
        List<ServiceRevenueData> revenueByService
) {}
