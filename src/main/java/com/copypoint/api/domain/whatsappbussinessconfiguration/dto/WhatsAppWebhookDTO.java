package com.copypoint.api.domain.whatsappbussinessconfiguration.dto;

import java.util.List;

public record WhatsAppWebhookDTO(
        String object,
        List<WhatsAppEntryDTO> entry
) {
}
