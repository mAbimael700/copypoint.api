package com.copypoint.api.infra.whatsappbusiness.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WhatsAppAudioDTO(
        String id,
        @JsonProperty("mime_type") String mimeType,
        String sha256,
        String caption
) {
}
