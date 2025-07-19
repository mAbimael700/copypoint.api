package com.copypoint.api.domain.whatsappbussinessconfiguration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record WhatsAppSendMessageResponseDTO(
        @JsonProperty("messaging_product") String messagingProduct,
        List<WhatsAppContactDTO> contacts,
        List<WhatsAppMessageResponseDTO> messages
) {
}
