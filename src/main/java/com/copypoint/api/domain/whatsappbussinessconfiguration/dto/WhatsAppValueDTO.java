package com.copypoint.api.domain.whatsappbussinessconfiguration.dto;

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
