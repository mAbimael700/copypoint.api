package com.copypoint.api.domain.whatsappbussinessconfiguration.dto;

import java.util.List;

public record WhatsAppEntryDTO(
        String id,
        List<WhatsAppChangeDTO> changes
) {}
