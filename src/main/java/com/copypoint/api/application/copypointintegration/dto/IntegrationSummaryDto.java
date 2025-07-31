package com.copypoint.api.application.copypointintegration.dto;

import java.util.List;

public record IntegrationSummaryDto(
        Long copypointId,
         String copypointName,
         List<PaymentIntegrationDto> paymentIntegrations,
         List<MessagingIntegrationDto> messagingIntegrations,
         IntegrationStatsDto stats
) {
}
