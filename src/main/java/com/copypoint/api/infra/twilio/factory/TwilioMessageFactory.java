package com.copypoint.api.infra.twilio.factory;

import com.copypoint.api.domain.messaging.MessagingProviderConfig;
import com.copypoint.api.domain.twilioconfiguration.TwilioConfiguration;
import com.copypoint.api.infra.twilio.config.TwilioProperties;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Component
public class TwilioMessageFactory {

    @Autowired
    private TwilioProperties twilioProperties;

    public MessageCreator createMessage(String to,
                                        String message,
                                        TwilioConfiguration twilioConfiguration) {
        return Message.creator(
                        new PhoneNumber(formatPhoneNumber(to)),
                        new PhoneNumber(formatPhoneNumber(getFromNumber(twilioConfiguration))),
                        message
                )
                .setStatusCallback(URI.create(buildWebhookUrl("/webhook/twilio/status")));
    }

    public MessageCreator createMediaMessage(String to,
                                             String mediaUrl,
                                             String caption,
                                             TwilioConfiguration twilioConfiguration) {

        List<URI> mediaUris = new ArrayList<>();
        if (StringUtils.hasText(mediaUrl)) {
            mediaUris.add(URI.create(mediaUrl));
        }

        return Message.creator(
                        new PhoneNumber(formatPhoneNumber(to)),
                        new PhoneNumber(formatPhoneNumber(getFromNumber(twilioConfiguration))),
                        StringUtils.hasText(caption) ? caption : ""
                )
                .setMediaUrl(mediaUris)
                .setStatusCallback(URI.create(buildWebhookUrl("/webhook/twilio/status")));
    }


    private String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            throw new IllegalArgumentException("Phone number cannot be null");
        }

        // Remover espacios y caracteres especiales
        String cleaned = phoneNumber.replaceAll("[^+\\d]", "");

        // Agregar + si no lo tiene
        if (!cleaned.startsWith("+")) {
            cleaned = "+" + cleaned;
        }

        return cleaned;
    }

    private String getFromNumber(TwilioConfiguration config) {
        return config.getCustomerServicePhone().getPhoneNumber();
    }

    private String buildWebhookUrl(String path) {
        return twilioProperties.getWebhook().getBaseUrl() + path;
    }

}
