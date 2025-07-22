package com.copypoint.api.domain.whatsappbussinessconfiguration.dto;

import com.copypoint.api.domain.whatsappbussinessconfiguration.WhatsAppBusinessConfiguration;

public record WhatsAppConfigurationResponseDTO(
        Long id,
        String phoneNumberId,
        String businessAccountId,
        String displayName,
        Boolean isActive,
        String webhookUrl
) {
    public WhatsAppConfigurationResponseDTO(WhatsAppBusinessConfiguration whatsAppBusinessConfiguration) {
        this(
                whatsAppBusinessConfiguration.getId(),
                whatsAppBusinessConfiguration.getPhoneNumberId(),
                whatsAppBusinessConfiguration.getBusinessAccountId(),
                whatsAppBusinessConfiguration.getDisplayName(),
                whatsAppBusinessConfiguration.getIsActive(),
                ""
        );
    }
}
