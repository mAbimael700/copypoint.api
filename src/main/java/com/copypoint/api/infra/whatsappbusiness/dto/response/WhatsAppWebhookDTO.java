package com.copypoint.api.infra.whatsappbusiness.dto.response;

import java.util.List;

public record WhatsAppWebhookDTO(
        String object,
        List<WhatsAppEntryDTO> entry
) {
}
