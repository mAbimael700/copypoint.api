package com.copypoint.api.domain.mercadopagoconfiguration.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record MercadoPagoConfigurationCreationDTO(
        @NotNull
        @NotEmpty
        String accessToken,

        @NotNull
        @NotEmpty
        String publicKey,

        @NotNull
        @NotEmpty
        String clientId,

        @NotNull
        @NotEmpty
        String clientSecret,

        @NotNull
        @NotEmpty
        String webhookSecret,

        Boolean isSandbox,

        @NotNull
        @NotEmpty
        @Email
        String vendorEmail
) {
}
