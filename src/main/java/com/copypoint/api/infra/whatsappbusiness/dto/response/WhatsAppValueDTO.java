package com.copypoint.api.infra.whatsappbusiness.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record WhatsAppValueDTO(
        @JsonProperty("messaging_product") String messagingProduct,
        WhatsAppMetadataDTO metadata,
        List<WhatsAppContactDTO> contacts,
        List<WhatsAppMessageDTO> messages,
        List<WhatsAppStatusDTO> statuses
) {
}
