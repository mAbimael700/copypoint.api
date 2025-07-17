package com.copypoint.api.domain.messagingproviderconfiguration.service;

import com.copypoint.api.domain.messagingproviderconfiguration.MessagingProviderConfiguration;
import com.copypoint.api.domain.messagingproviderconfiguration.MessagingProviderType;

public interface MessagingService {
    boolean sendMessage(String to, String message, MessagingProviderConfiguration config);

    boolean sendMediaMessage(String to, String mediaUrl, String caption, MessagingProviderConfiguration config);

    void configureWebhook(MessagingProviderConfiguration config);

    boolean validateConfiguration(MessagingProviderConfiguration config);

    MessagingProviderType getSupportedProviderType();
}
