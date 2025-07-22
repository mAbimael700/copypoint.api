package com.copypoint.api.domain.twilioconfiguration.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record TwilioConfigurationCreationRequest(
        @NotNull
        @NotEmpty
        String accountSid,

        @NotNull
        @NotEmpty
        String authToken,

        @NotNull
        @NotEmpty
        String webhookUrl,

        @NotNull
        @NotEmpty
        String statusCallbackUrl,

        @NotNull
        @NotEmpty
        String displayName,

        @NotNull
        Boolean isActive
) {
}
