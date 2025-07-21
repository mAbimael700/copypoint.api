package com.copypoint.api.infra.whatsappbusiness.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WhatsAppContactDTO(
        WhatsAppProfileDTO profile,
        @JsonProperty("wa_id") String waId
) {
}
