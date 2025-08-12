package com.copypoint.api.dashboard.dto;

import java.time.LocalDate;

public record SalesTimelineData(
        LocalDate date,
        Double totalSales,
        Integer transactionCount
) {
}

