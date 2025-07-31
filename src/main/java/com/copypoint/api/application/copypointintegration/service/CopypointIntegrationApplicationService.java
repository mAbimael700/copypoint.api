package com.copypoint.api.application.copypointintegration.service;

import com.copypoint.api.application.copypointintegration.dto.IntegrationStatsDto;
import com.copypoint.api.application.copypointintegration.dto.IntegrationSummaryDto;
import com.copypoint.api.application.copypointintegration.dto.MessagingIntegrationDto;
import com.copypoint.api.application.copypointintegration.dto.PaymentIntegrationDto;
import com.copypoint.api.application.copypointintegration.exception.CopypointNotFoundException;
import com.copypoint.api.domain.copypoint.Copypoint;
import com.copypoint.api.domain.copypoint.repository.CopypointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CopypointIntegrationApplicationService {
    @Autowired
    private CopypointRepository copypointRepository;

    @Autowired
    private  IntegrationService integrationService;

    public IntegrationSummaryDto getCopypointIntegrations(Long copypointId) {
        Copypoint copypoint = copypointRepository.findById(copypointId)
                .orElseThrow(() -> new CopypointNotFoundException("Copypoint not found with id: " + copypointId));

        List<PaymentIntegrationDto> paymentIntegrations =
                integrationService.getPaymentIntegrations(copypoint);

        List<MessagingIntegrationDto> messagingIntegrations =
                integrationService.getMessagingIntegrations(copypoint);

        IntegrationStatsDto stats = integrationService.calculateStats(paymentIntegrations, messagingIntegrations);

        return new IntegrationSummaryDto(
                copypoint.getId(),
                copypoint.getName(),
                paymentIntegrations,
                messagingIntegrations,
                stats
        );
    }

    // Métodos adicionales para consultas específicas
    public List<PaymentIntegrationDto> getActivePaymentIntegrations(Long copypointId) {
        Copypoint copypoint = findCopypoint(copypointId);
        return integrationService.getPaymentIntegrations(copypoint)
                .stream()
                .filter(PaymentIntegrationDto::isActive)
                .toList();
    }

    public List<MessagingIntegrationDto> getActiveMessagingIntegrations(Long copypointId) {
        Copypoint copypoint = findCopypoint(copypointId);
        return integrationService.getMessagingIntegrations(copypoint)
                .stream()
                .filter(MessagingIntegrationDto::isActive)
                .toList();
    }

    public boolean hasActiveIntegrations(Long copypointId) {
        IntegrationSummaryDto summary = getCopypointIntegrations(copypointId);
        return summary.stats().activeIntegrations() > 0;
    }

    public IntegrationStatsDto getIntegrationStats(Long copypointId) {
        IntegrationSummaryDto summary = getCopypointIntegrations(copypointId);
        return summary.stats();
    }

    private Copypoint findCopypoint(Long copypointId) {
        return copypointRepository.findById(copypointId)
                .orElseThrow(() -> new CopypointNotFoundException("Copypoint not found with id: " + copypointId));
    }

}
