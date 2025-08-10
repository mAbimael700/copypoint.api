package com.copypoint.api.domain.attachment.dto;

import com.copypoint.api.domain.attachment.Attachment;
import com.copypoint.api.domain.attachment.AttachmentFileType;

public record AttachmentResponse(
        Long id,
        AttachmentFileType fileType,
        String originalName,
        String mediaSid,
        Long messageId

) {

    public AttachmentResponse(Attachment attachment) {
        this(
                attachment.getId(),
                attachment.getFileType(),
                attachment.getOriginalName(),
                attachment.getMediaSid(),
                attachment.getMessage().getId()
        );
    }
}
