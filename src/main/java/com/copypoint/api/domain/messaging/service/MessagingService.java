package com.copypoint.api.domain.messaging.service;

import com.copypoint.api.domain.messaging.MessagingProviderConfig;
import com.copypoint.api.domain.messaging.MessagingProviderType;

public interface MessagingService {
    boolean sendMessage(String to, String message, MessagingProviderConfig config);

    boolean sendMediaMessage(String to, String mediaUrl, String caption, MessagingProviderConfig config);

    void configureWebhook(MessagingProviderConfig config);

    boolean validateConfiguration(MessagingProviderConfig config);

    MessagingProviderType getSupportedProviderType();
}
