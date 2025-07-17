package com.copypoint.api.infra.twilio.dto;

import java.time.LocalDateTime;

public record TwilioMessageResponse(
        String messageId,
        String status,
        String to,
        String from,
        String body,
        LocalDateTime sentAt,
        String errorCode,
        String errorMessage,
        boolean success
) {
}
