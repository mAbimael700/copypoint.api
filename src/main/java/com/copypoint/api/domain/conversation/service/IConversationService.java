package com.copypoint.api.domain.conversation.service;

import com.copypoint.api.domain.contact.Contact;
import com.copypoint.api.domain.conversation.Conversation;
import com.copypoint.api.domain.customerservicephone.CustomerServicePhone;

public interface IConversationService {
    Conversation findOrCreateConversation(Contact contact, CustomerServicePhone phone);
}
