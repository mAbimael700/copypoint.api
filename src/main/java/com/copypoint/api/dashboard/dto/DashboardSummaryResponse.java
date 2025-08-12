package com.copypoint.api.dashboard.dto;

import java.util.List;

public record DashboardSummaryResponse(
        SalesMetrics salesMetrics,
        PaymentStatusMetrics paymentMetrics,
        List<ServiceSalesData> topServices,
        CopypointMetrics copypointMetrics
) {
}
