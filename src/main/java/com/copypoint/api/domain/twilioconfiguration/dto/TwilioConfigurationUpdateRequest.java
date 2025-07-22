package com.copypoint.api.domain.twilioconfiguration.dto;

public record TwilioConfigurationUpdateRequest(
        String accountSid,
        String authToken,
        String webhookUrl,
        String statusCallbackUrl,
        String displayName,
        Boolean isActive
) {
}
