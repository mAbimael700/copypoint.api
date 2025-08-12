package com.copypoint.api.dashboard.dto;

public record CopypointPerformanceData(
        Long copypointId,
        String copypointName,
        Double totalSales,
        Integer transactionCount,
        Double averagePerTransaction
) {}
