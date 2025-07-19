package com.copypoint.api.domain.message.service;

import com.copypoint.api.domain.message.Message;

public interface IMessageService {
    Message save(Message message);
    Message findByMessageSid(String messageSid);
}
