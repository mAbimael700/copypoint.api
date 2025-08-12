package com.copypoint.api.dashboard.dto;

public record ServiceSalesData(
        Long serviceId,
        String serviceName,
        Integer quantitySold,
        Double totalRevenue
) {}
