package com.copypoint.api.dashboard.dto;

import java.util.List;

public record SalesTimelineResponse(
        List<SalesTimelineData> timeline,
        SalesMetrics metrics
) {
}
