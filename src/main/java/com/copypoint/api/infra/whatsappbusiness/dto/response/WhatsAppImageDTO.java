package com.copypoint.api.infra.whatsappbusiness.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

// Media DTOs
public record WhatsAppImageDTO(
        String id,
        @JsonProperty("mime_type") String mimeType,
        String sha256,
        String caption
) {
}
