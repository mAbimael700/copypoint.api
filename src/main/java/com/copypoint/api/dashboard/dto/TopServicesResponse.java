package com.copypoint.api.dashboard.dto;

import java.util.List;

// DTOs para Dashboard de Servicios Más Vendidos
public record TopServicesResponse(
        List<ServiceSalesData> topServices,
        ServiceMetrics metrics
) {}
