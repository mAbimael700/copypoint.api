package com.copypoint.api.application.copypointintegration.controller;

import com.copypoint.api.application.copypointintegration.dto.IntegrationStatsDto;
import com.copypoint.api.application.copypointintegration.dto.IntegrationSummaryDto;
import com.copypoint.api.application.copypointintegration.dto.MessagingIntegrationDto;
import com.copypoint.api.application.copypointintegration.dto.PaymentIntegrationDto;
import com.copypoint.api.application.copypointintegration.service.CopypointIntegrationApplicationService;
import com.copypoint.api.application.copypointintegration.exception.CopypointNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/copypoints/{copypointId}/integrations")
@Slf4j
public class IntegrationController {

    @Autowired
    private CopypointIntegrationApplicationService integrationService;

    @GetMapping
    public ResponseEntity<IntegrationSummaryDto> getCopypointIntegrations(
            @PathVariable Long copypointId) {

        try {
            log.info("Obteniendo integraciones para copypoint: {}", copypointId);
            IntegrationSummaryDto integrations = integrationService.getCopypointIntegrations(copypointId);
            log.info("Integraciones encontradas para copypoint {}: {} total, {} activas",
                    copypointId, integrations.stats().totalIntegrations(), integrations.stats().activeIntegrations());
            return ResponseEntity.ok(integrations);
        } catch (CopypointNotFoundException e) {
            log.warn("Copypoint no encontrado: {}", copypointId);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/payment")
    public ResponseEntity<List<PaymentIntegrationDto>> getActivePaymentIntegrations(
            @PathVariable Long copypointId) {

        try {
            List<PaymentIntegrationDto> paymentIntegrations =
                    integrationService.getActivePaymentIntegrations(copypointId);
            return ResponseEntity.ok(paymentIntegrations);
        } catch (CopypointNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/messaging")
    public ResponseEntity<List<MessagingIntegrationDto>> getActiveMessagingIntegrations(
            @PathVariable Long copypointId) {

        try {
            List<MessagingIntegrationDto> messagingIntegrations =
                    integrationService.getActiveMessagingIntegrations(copypointId);
            return ResponseEntity.ok(messagingIntegrations);
        } catch (CopypointNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<IntegrationStatsDto> getIntegrationStats(
            @PathVariable Long copypointId) {

        try {
            IntegrationStatsDto stats = integrationService.getIntegrationStats(copypointId);
            return ResponseEntity.ok(stats);
        } catch (CopypointNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Boolean> hasActiveIntegrations(
            @PathVariable Long copypointId) {

        try {
            boolean hasActive = integrationService.hasActiveIntegrations(copypointId);
            return ResponseEntity.ok(hasActive);
        } catch (CopypointNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
