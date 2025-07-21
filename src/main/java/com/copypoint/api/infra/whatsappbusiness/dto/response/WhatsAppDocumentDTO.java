package com.copypoint.api.infra.whatsappbusiness.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WhatsAppDocumentDTO(
        String id,
        @JsonProperty("mime_type") String mimeType,
        String sha256,
        String filename,
        String caption
) {
}
