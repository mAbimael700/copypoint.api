package com.copypoint.api.dashboard.dto;

import java.time.LocalDate;

public record DateRangeValidationResponse(
        Boolean isValid,
        String message,
        LocalDate startDate,
        LocalDate endDate
) {
}
