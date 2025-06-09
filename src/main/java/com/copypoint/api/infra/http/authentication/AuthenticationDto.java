package com.copypoint.api.infra.http.authentication;

public record AuthenticationDto(
        String email,
        String password
) {
}
