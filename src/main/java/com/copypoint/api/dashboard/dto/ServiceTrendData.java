package com.copypoint.api.dashboard.dto;

import java.time.LocalDate;

public record ServiceTrendData(
        LocalDate date,
        String serviceName,
        Integer quantitySold,
        Double revenue
) {}
