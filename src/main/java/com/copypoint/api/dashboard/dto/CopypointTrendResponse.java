package com.copypoint.api.dashboard.dto;

import java.util.List;

public record CopypointTrendResponse(
        List<CopypointTrendData> trends
) {}
