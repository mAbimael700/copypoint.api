package com.copypoint.api.infra.whatsappbusiness.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WhatsAppStatusDTO(
        String id,
        String status,
        String timestamp,
        @JsonProperty("recipient_id") String recipientId
) {
}
