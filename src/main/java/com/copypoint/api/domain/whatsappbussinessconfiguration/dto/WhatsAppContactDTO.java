package com.copypoint.api.domain.whatsappbussinessconfiguration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WhatsAppContactDTO(
        WhatsAppProfileDTO profile,
        @JsonProperty("wa_id") String waId
) {
}
