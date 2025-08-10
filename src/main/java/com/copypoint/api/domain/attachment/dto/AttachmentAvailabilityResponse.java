package com.copypoint.api.domain.attachment.dto;

public record AttachmentAvailabilityResponse(
        Long attachmentId,
        Boolean isAvailable
) {
}
