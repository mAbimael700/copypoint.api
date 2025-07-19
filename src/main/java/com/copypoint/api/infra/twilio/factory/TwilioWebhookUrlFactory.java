package com.copypoint.api.infra.twilio.factory;

import com.copypoint.api.infra.twilio.config.TwilioProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class TwilioWebhookUrlFactory {
    @Autowired
    private TwilioProperties twilioProperties;


    public String buildWebhookUrl(String path) {
        return twilioProperties.getWebhook().getBaseUrl() + "/webhook/twilio/whatsapp" + path;
    }

}
