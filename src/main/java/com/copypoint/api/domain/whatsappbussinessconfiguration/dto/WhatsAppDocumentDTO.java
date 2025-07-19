package com.copypoint.api.domain.whatsappbussinessconfiguration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WhatsAppDocumentDTO(
        String id,
        @JsonProperty("mime_type") String mimeType,
        String sha256,
        String filename,
        String caption
) {
}
