package com.copypoint.api.dashboard.dto;

public record SalesByCopypointData(
        Long copypointId,
        String copypointName,
        Double totalSales,
        Integer transactionCount
) {}
