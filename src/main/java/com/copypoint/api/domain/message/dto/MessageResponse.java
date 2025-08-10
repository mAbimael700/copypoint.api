package com.copypoint.api.domain.message.dto;

import com.copypoint.api.domain.attachment.dto.AttachmentResponse;
import com.copypoint.api.domain.message.Message;
import com.copypoint.api.domain.message.MessageDirection;
import com.copypoint.api.domain.message.MessageStatus;

import java.util.List;

public record MessageResponse(
        Long id,
        String messageSiD,
        MessageDirection direction,
        MessageStatus status,
        String body,
        Long conversationId,
        List<AttachmentResponse> attachments
) {

    public MessageResponse(Message message) {
        this(
                message.getId(),
                message.getMessageSid(),
                message.getDirection(),
                message.getStatus(),
                message.getBody(),
                message.getConversation().getId(),
                message.getAttachments().stream()
                        .map(AttachmentResponse::new).toList()
        );
    }
}
