package com.copypoint.api.domain.message.dto;

import com.copypoint.api.domain.message.Message;

public record MessageResponse() {

    public MessageResponse(Message message) {
        this();
    }
}
