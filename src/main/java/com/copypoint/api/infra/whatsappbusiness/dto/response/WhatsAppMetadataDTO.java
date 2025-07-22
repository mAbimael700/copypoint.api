package com.copypoint.api.infra.whatsappbusiness.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WhatsAppMetadataDTO(
        @JsonProperty("display_phone_number") String displayPhoneNumber,
        @JsonProperty("phone_number_id") String phoneNumberId
) {
}
