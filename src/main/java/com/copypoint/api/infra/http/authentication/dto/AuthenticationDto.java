package com.copypoint.api.infra.http.authentication.dto;

public record AuthenticationDto(
        String email,
        String password
) {
}
