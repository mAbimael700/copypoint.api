package com.copypoint.api.domain.whatsappbussinessconfiguration.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record WhatsAppConfigurationDTO(
        @NotNull
        @NotEmpty
        String accessToken,

        @NotNull
        @NotEmpty
        String phoneNumberId,

        @NotNull
        @NotEmpty
        String businessAccountId,

        @NotNull
        @NotEmpty
        String webhookVerifyToken,

        @NotNull
        @NotEmpty
        String appId,

        @NotNull
        @NotEmpty
        String appSecret,

        @NotNull
        @NotEmpty
        String displayName,

        Boolean isActive
) {
}
