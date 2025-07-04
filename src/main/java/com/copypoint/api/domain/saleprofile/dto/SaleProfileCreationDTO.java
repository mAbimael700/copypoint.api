package com.copypoint.api.domain.saleprofile.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SaleProfileCreationDTO(
        @NotNull
        @Positive
        Long profileId,
        @NotNull
        @Positive
        Long serviceId,
        @NotNull
        @Positive
        Integer quantity
) {
}
