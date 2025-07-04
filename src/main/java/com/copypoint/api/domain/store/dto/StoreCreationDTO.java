package com.copypoint.api.domain.store.dto;

import jakarta.validation.constraints.Size;

public record StoreCreationDTO(
        String name,

        @Size(min = 3, max = 3)
        String currency
) {
}
