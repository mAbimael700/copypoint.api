package com.copypoint.api.application.copypointintegration.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public sealed interface BaseIntegrationDto
        permits PaymentIntegrationDto, MessagingIntegrationDto {

    Long id();
    String providerName();
    String displayName();
    boolean isActive();
    boolean isConfigured();

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdAt();

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime updatedAt();
}
