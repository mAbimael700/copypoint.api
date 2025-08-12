package com.copypoint.api.dashboard.dto;

import java.util.List;

public record ServiceTrendResponse(
        List<ServiceTrendData> trends
) {}
