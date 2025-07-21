package com.copypoint.api.infra.whatsappbusiness.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record WhatsAppSendMessageResponseDTO(
        @JsonProperty("messaging_product") String messagingProduct,
        List<WhatsAppContactDTO> contacts,
        List<WhatsAppMessageResponseDTO> messages
) {
}
