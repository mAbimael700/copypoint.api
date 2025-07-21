package com.copypoint.api.infra.whatsappbusiness.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

// DTOs para envío de mensajes
public record WhatsAppSendMessageRequestDTO(
        @JsonProperty("messaging_product") String messagingProduct,
        String to,
        String type,
        WhatsAppTextDTO text,
        WhatsAppImageDTO image
) {
    public static WhatsAppSendMessageRequestDTO createTextMessage(String to, String message) {
        return new WhatsAppSendMessageRequestDTO(
                "whatsapp",
                to,
                "text",
                new WhatsAppTextDTO(message),
                null
        );
    }

    public static WhatsAppSendMessageRequestDTO createImageMessage(String to, String imageId, String caption) {
        return new WhatsAppSendMessageRequestDTO(
                "whatsapp",
                to,
                "image",
                null,
                new WhatsAppImageDTO(imageId, null, null, caption)
        );
    }
}
