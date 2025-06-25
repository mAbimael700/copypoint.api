package com.copypoint.api.infra.http.userprofile.dto;

public record UserProfileResponseDTO(
        Long id,
        String name,
        String email,
        Long exp // Timestamp de expiración del token
) {
}
