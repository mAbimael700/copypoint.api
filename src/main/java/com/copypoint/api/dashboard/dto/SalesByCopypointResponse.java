package com.copypoint.api.dashboard.dto;

import java.util.List;

public record SalesByCopypointResponse(
        List<SalesByCopypointData> salesByLocation,
        SalesMetrics globalMetrics
) {}
