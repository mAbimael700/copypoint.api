package com.copypoint.api.domain.message.controller;

import com.copypoint.api.domain.message.dto.MessageResponse;
import com.copypoint.api.domain.message.service.MessageService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<Page<MessageResponse>> getMessagesByConversation(
            @PathVariable Long conversationId,
            Pageable pageable
    ) {
        Page<MessageResponse> messages = messageService
                .getByConversation(conversationId, pageable).map(MessageResponse::new);

        return ResponseEntity.ok(messages);
    }
}
