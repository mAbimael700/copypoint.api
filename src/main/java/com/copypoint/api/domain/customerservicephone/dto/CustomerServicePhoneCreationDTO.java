package com.copypoint.api.domain.customerservicephone.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CustomerServicePhoneCreationDTO(
        @NotNull
        @NotEmpty
        String phoneNumber
) {

}
