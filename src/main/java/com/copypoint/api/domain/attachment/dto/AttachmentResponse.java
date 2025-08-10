package com.copypoint.api.domain.attachment.dto;

import com.copypoint.api.domain.attachment.Attachment;
import com.copypoint.api.domain.attachment.AttachmentDownloadStatus;
import com.copypoint.api.domain.attachment.AttachmentFileType;

import java.time.LocalDateTime;

public record AttachmentResponse(
        Long id,
        String originalName,
        AttachmentFileType fileType,
        Long messageId,
        String mimeType,
        Long fileSizeBytes,
        AttachmentDownloadStatus downloadStatus,
        Boolean isAvailable,
        LocalDateTime dateCreated,
        LocalDateTime dateDownloaded

) {

    public AttachmentResponse(Attachment attachment) {
        this(
                attachment.getId(),
                attachment.getOriginalName(),
                attachment.getFileType(),
                attachment.getMessage().getId(),
                attachment.getMimeType(),
                attachment.getFileSizeBytes(),
                attachment.getDownloadStatus(),
                attachment.isDownloaded(),
                attachment.getDateCreated(),
                attachment.getDateUpdated()
        );
    }
}
