package com.copypoint.api.domain.message.service;

import com.copypoint.api.domain.conversation.Conversation;
import com.copypoint.api.domain.message.Message;
import com.copypoint.api.domain.message.MessageDirection;
import com.copypoint.api.domain.message.MessageStatus;
import com.copypoint.api.domain.message.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService implements IMessageService {

    @Autowired
    private MessageRepository messageRepository;


    public Message createInboundMessage(
            String messageSid,
            List<String> mediaUrls,
            Conversation conversation
    ) {
        return createMessage(
                messageSid,
                MessageDirection.INBOUND,
                MessageStatus.RECEIVED,
                mediaUrls,
                conversation
        );
    }

    public Message createOutboundMessage(
            String messageSid,
            List<String> mediaUrls,
            Conversation conversation
    ) {
        return createMessage(
                messageSid,
                MessageDirection.OUTBOUND,
                MessageStatus.QUEUED,
                mediaUrls,
                conversation
        );
    }

    public Page<Message> getByConversation(Long conversationId, Pageable pageable) {
        return messageRepository.findByConversationIdOrderByDateSent(conversationId, pageable);
    }

    private Message createMessage(
            String messageSid,
            MessageDirection direction,
            MessageStatus status,
            List<String> mediaUrls,
            Conversation conversation
    ) {
        Message newMessage = Message.builder()
                .messageSid(messageSid)
                .direction(direction)
                .status(status)
                .mediaUrls(mediaUrls)
                .conversation(conversation)
                .build();

        return messageRepository.save(newMessage);
    }

    @Override
    public Message save(Message message) {
        return messageRepository.save(message);
    }

    @Override
    public Message findByMessageSid(String messageSid) {
        return messageRepository.findByMessageSid(messageSid);
    }
}
