package com.copypoint.api.infra.whatsappbusiness.dto.response;

import java.time.Instant;

public record WhatsAppMessageDTO(
        String from,
        String id,
        String timestamp,
        WhatsAppTextDTO text,
        String type,
        WhatsAppContextDTO context,
        WhatsAppImageDTO image,
        WhatsAppVideoDTO video,
        WhatsAppAudioDTO audio,
        WhatsAppDocumentDTO document
) {
    public Instant getTimestampAsInstant() {
        return timestamp != null ? Instant.ofEpochSecond(Long.parseLong(timestamp)) : null;
    }
}
