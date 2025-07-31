package com.copypoint.api.application.copypointintegration.dto;

import com.copypoint.api.domain.messagingproviderconfiguration.MessagingProviderType;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record MessagingIntegrationDto(
        Long id,
        String providerName,
        String displayName,
        boolean isActive,
        boolean isConfigured,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime updatedAt,
        MessagingProviderType providerType,
        String phoneNumber,
        String businessAccountId,
        String phoneNumberId
) implements BaseIntegrationDto {
    // Constructor compacto para validaciones
    public MessagingIntegrationDto {
        if (providerName == null || providerName.isBlank()) {
            throw new IllegalArgumentException("Provider name cannot be null or blank");
        }
        if (providerType == null) {
            throw new IllegalArgumentException("Provider type cannot be null");
        }
    }

    // Método factory para crear desde WhatsApp
    public static MessagingIntegrationDto fromWhatsAppConfiguration(
            Long id,
            boolean isActive,
            boolean isConfigured,
            String phoneNumber,
            String businessAccountId,
            String phoneNumberId) {

        return new MessagingIntegrationDto(
                id, "WhatsApp Business", "WhatsApp Business API",
                isActive, isConfigured, null, null,
                MessagingProviderType.WHATSAPP_BUSINESS_API,
                phoneNumber, businessAccountId, phoneNumberId
        );
    }

    // Método factory para crear desde Twilio
    public static MessagingIntegrationDto fromTwilioConfiguration(
            Long id,
            boolean isActive,
            boolean isConfigured,
            String phoneNumber) {

        return new MessagingIntegrationDto(
                id, "Twilio", "Twilio SMS/WhatsApp",
                isActive, isConfigured, null, null,
                MessagingProviderType.TWILIO,
                phoneNumber, null, null
        );
    }

    // Método helper para verificar si es WhatsApp
    public boolean isWhatsApp() {
        return providerType == MessagingProviderType.WHATSAPP_BUSINESS_API;
    }

    // Método helper para verificar si es Twilio
    public boolean isTwilio() {
        return providerType == MessagingProviderType.TWILIO;
    }
}
