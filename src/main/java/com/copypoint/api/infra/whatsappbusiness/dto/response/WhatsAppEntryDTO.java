package com.copypoint.api.infra.whatsappbusiness.dto.response;

import java.util.List;

public record WhatsAppEntryDTO(
        String id,
        List<WhatsAppChangeDTO> changes
) {}
