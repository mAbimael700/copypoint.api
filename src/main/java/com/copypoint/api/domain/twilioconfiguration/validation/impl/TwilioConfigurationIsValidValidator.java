package com.copypoint.api.domain.twilioconfiguration.validation.impl;

import com.copypoint.api.domain.messaging.MessagingProviderConfig;
import com.copypoint.api.domain.twilioconfiguration.exceptions.TwilioConfigurationException;
import com.copypoint.api.domain.twilioconfiguration.validation.TwilioConfigurationValidator;
import org.springframework.stereotype.Component;

@Component
public class TwilioConfigurationIsValidValidator implements TwilioConfigurationValidator {
    @Override
    public void valid(MessagingProviderConfig config) throws TwilioConfigurationException {
        if (!config.isConfigurationValid()) {
            throw new TwilioConfigurationException("Invalid Twilio configuration");
        }
    }
}
