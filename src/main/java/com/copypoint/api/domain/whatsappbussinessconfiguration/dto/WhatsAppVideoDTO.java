package com.copypoint.api.domain.whatsappbussinessconfiguration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WhatsAppVideoDTO(
        String id,
        @JsonProperty("mime_type") String mimeType,
        String sha256,
        String caption
) {
}
