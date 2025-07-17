package com.copypoint.api.infra.twilio.dto;

import java.time.LocalDateTime;

public record TwilioWebhookMessage(
        String messageSid,
        String messageStatus,
        String to,
        String from,
        String body,
        String mediaUrl,
        String mediaContentType,
        LocalDateTime timestamp
) {
}
