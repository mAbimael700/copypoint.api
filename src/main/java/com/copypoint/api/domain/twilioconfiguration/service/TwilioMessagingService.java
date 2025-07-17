package com.copypoint.api.domain.twilioconfiguration.service;

import com.copypoint.api.domain.messaging.MessagingProviderConfig;
import com.copypoint.api.domain.messaging.MessagingProviderType;
import com.copypoint.api.domain.messaging.service.MessagingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TwilioMessagingService implements MessagingService {

    @Override
    public boolean sendMessage(String to, String message, MessagingProviderConfig config) {
        return false;
    }

    @Override
    public boolean sendMediaMessage(String to, String mediaUrl, String caption, MessagingProviderConfig config) {
        return false;
    }

    @Override
    public void configureWebhook(MessagingProviderConfig config) {

    }

    @Override
    public boolean validateConfiguration(MessagingProviderConfig config) {
        return false;
    }

    @Override
    public MessagingProviderType getSupportedProviderType() {
        return null;
    }
}
