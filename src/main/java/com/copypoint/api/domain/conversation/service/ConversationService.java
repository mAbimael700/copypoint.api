package com.copypoint.api.domain.conversation.service;

import com.copypoint.api.domain.contact.Contact;
import com.copypoint.api.domain.conversation.Conversation;
import com.copypoint.api.domain.conversation.repository.ConversationRepository;
import com.copypoint.api.domain.customerservicephone.CustomerServicePhone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ConversationService {

    @Autowired
    private ConversationRepository conversationRepository;

    public Conversation createConversation(
            CustomerServicePhone customerServicePhone,
            Contact customerContact
    ) {

        Conversation conversation = Conversation.builder()
                .customerContact(customerContact)
                .customerServicePhone(customerServicePhone)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();

        return conversationRepository.save(conversation);
    }

    public Page<Conversation> getByCustomerContact(Long customerServicePhoneId,
                                                   Pageable pageable) {
        return conversationRepository
                .findByCustomerServicePhoneIdOrderByCreatedAtDesc(
                        customerServicePhoneId,
                        pageable);
    }

    public Conversation getCompleteConversation(Long conversationId) {
        return conversationRepository
                .findConversationWithDetailsByConversationId(conversationId);
    }

    public boolean conversationExists(
            String customerPhoneNumber,
            String customerServicePhoneNumber
    ) {
        return conversationRepository.existsByCustomerPhoneNumberAndCustomerServicePhoneNumber(
                customerPhoneNumber, customerServicePhoneNumber);
    }
}
