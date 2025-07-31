package com.copypoint.api.application.copypointintegration.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record   PaymentIntegrationDto(
        Long id,
        String providerName,
        String displayName,
        boolean isActive,
        boolean isConfigured,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime updatedAt,
        String providerType,
        String clientId,
        String vendorEmail,
        boolean isSandbox
) implements BaseIntegrationDto {

    // Constructor compacto para validaciones
    public PaymentIntegrationDto {
        if (providerName == null || providerName.isBlank()) {
            throw new IllegalArgumentException("Provider name cannot be null or blank");
        }
        if (providerType == null || providerType.isBlank()) {
            throw new IllegalArgumentException("Provider type cannot be null or blank");
        }
    }

    // Método factory para crear desde entity
    public static PaymentIntegrationDto fromMercadoPagoConfiguration(
            Long id,
            String providerName,
            String displayName,
            boolean isActive,
            boolean isConfigured,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            String clientId,
            String vendorEmail,
            boolean isSandbox) {

        return new PaymentIntegrationDto(
                id, providerName, displayName, isActive, isConfigured,
                createdAt, updatedAt, "MERCADO_PAGO", clientId, vendorEmail, isSandbox
        );
    }

    // Método helper para verificar si es sandbox
    public boolean isProduction() {
        return !isSandbox;
    }
}
