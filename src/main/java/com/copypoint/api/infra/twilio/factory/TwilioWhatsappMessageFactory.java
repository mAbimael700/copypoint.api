package com.copypoint.api.infra.twilio.factory;

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
public class TwilioWhatsappMessageFactory {

    @Autowired
    private TwilioWebhookUrlFactory webhookUrlFactory;

    public MessageCreator createMessage(String to,
                                        String message,
                                        TwilioConfiguration twilioConfiguration) {
        return Message.creator(
                        new PhoneNumber(formatWhatsappPhoneNumber(to)),
                        new PhoneNumber(formatWhatsappPhoneNumber(getFromNumber(twilioConfiguration))),
                        message
                )
                .setStatusCallback(URI.create(webhookUrlFactory
                        .buildWebhookUrl("/webhook/twilio/whatsapp/status")));
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
                        new PhoneNumber(formatWhatsappPhoneNumber(to)),
                        new PhoneNumber(formatWhatsappPhoneNumber(getFromNumber(twilioConfiguration))),
                        StringUtils.hasText(caption) ? caption : ""
                )
                .setMediaUrl(mediaUris)
                .setStatusCallback(URI.create(webhookUrlFactory
                        .buildWebhookUrl("/webhook/twilio/whatsapp/status")));
    }


    public MessageCreator createWhatsAppTemplateMessage(String to,
                                                        String templateSid,
                                                        String contentVariables,
                                                        TwilioConfiguration twilioConfiguration) {
        MessageCreator creator = Message.creator(
                new PhoneNumber(formatWhatsappPhoneNumber(to)),
                new PhoneNumber(formatWhatsappPhoneNumber(getFromNumber(twilioConfiguration))),
                ""
        );

        // Para mensajes de template (mensajes pre-aprobados)
        if (StringUtils.hasText(templateSid)) {
            creator = creator.setContentSid(templateSid);
        }

        if (StringUtils.hasText(contentVariables)) {
            creator = creator.setContentVariables(contentVariables);
        }

        return creator.setStatusCallback(
                URI.create(webhookUrlFactory
                        .buildWebhookUrl("/webhook/twilio/whatsapp/status")));
    }

    private String formatWhatsappPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            throw new IllegalArgumentException("Phone number cannot be null");
        }

        // Si ya tiene el prefijo whatsapp:, devolverlo tal como está
        if (phoneNumber.startsWith("whatsapp:")) {
            return phoneNumber;
        }

        // Remover espacios y caracteres especiales
        String cleaned = phoneNumber.replaceAll("[^+\\d]", "");

        // Agregar + si no lo tiene
        if (!cleaned.startsWith("+")) {
            cleaned = "+" + cleaned;
        }

        // Agregar el prefijo de WhatsApp
        return "whatsapp:" + cleaned;
    }

    private String getFromNumber(TwilioConfiguration config) {
        return config.getCustomerServicePhone().getPhoneNumber();
    }

}
