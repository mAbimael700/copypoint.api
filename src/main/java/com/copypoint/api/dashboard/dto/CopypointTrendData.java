package com.copypoint.api.dashboard.dto;

import java.time.LocalDate;

public record CopypointTrendData(
        LocalDate date,
        Long copypointId,
        String copypointName,
        Double sales,
        Integer transactions
) {}
