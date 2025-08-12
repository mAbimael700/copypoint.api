package com.copypoint.api.dashboard.dto;

public record SalesMetrics(
        Double totalSales,
        Double averagePerTransaction,
        Integer totalTransactions
) {}
