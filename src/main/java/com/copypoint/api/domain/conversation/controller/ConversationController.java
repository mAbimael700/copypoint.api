package com.copypoint.api.domain.conversation.controller;

import com.copypoint.api.domain.conversation.Conversation;
import com.copypoint.api.domain.conversation.dto.ConversationResponse;
import com.copypoint.api.domain.conversation.service.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    @Autowired
    private ConversationService conversationService;

    @GetMapping("/phone/{phoneId}")
    public ResponseEntity<Page<ConversationResponse>> getConversationByCustomerServicePhone(
            @PathVariable Long phoneId,
            Pageable pageable
    ) {
        Page<Conversation> conversations = conversationService
                .getByCustomerContact(phoneId, pageable);

        return ResponseEntity.ok(conversations.map(ConversationResponse::new));
    }
}
