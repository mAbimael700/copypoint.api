package com.copypoint.api.dashboard.dto;

import java.util.List;

// DTOs para Dashboard de Performance por Copypoint
public record CopypointPerformanceResponse(
        List<CopypointPerformanceData> performance,
        CopypointMetrics metrics
) {}
