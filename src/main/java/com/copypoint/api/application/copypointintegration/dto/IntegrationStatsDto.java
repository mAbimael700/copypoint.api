package com.copypoint.api.application.copypointintegration.dto;

public record IntegrationStatsDto(
        Integer totalIntegrations,
        Integer activeIntegrations,
        Integer paymentProvidersCount,
        Integer messagingProvidersCount
) {
}
