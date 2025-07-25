package com.copypoint.api.domain.conversation.dto;

import com.copypoint.api.domain.conversation.Conversation;

import java.time.LocalDateTime;

public record ConversationResponse(
        Long id,
        LocalDateTime createdAt,
        Long phoneId,
        String phoneNumber,
        String displayName
) {

    public ConversationResponse(Conversation conversation) {
        this(
                conversation.getId(),
                conversation.getCreatedAt(),
                conversation.getCustomerServicePhone().getId(),
                conversation.getCustomerServicePhone().getPhoneNumber(),
                conversation.getCustomerServicePhone().getMessagingConfig().getDisplayName()

        );
    }
}
