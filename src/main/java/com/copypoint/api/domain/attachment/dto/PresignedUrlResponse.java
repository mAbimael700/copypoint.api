package com.copypoint.api.domain.attachment.dto;

public record PresignedUrlResponse(
        Long attachmentId,
        String presignedUrl,
        Integer expirationMinutes
) {
}
