package com.copypoint.api.domain.saleprofile.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SaleProfileUpdateDTO(
        @NotNull(message = "Quantity can not be null")
        @Positive(message = "Quantity must be higher than 0")
        Integer quantity
) {
}
