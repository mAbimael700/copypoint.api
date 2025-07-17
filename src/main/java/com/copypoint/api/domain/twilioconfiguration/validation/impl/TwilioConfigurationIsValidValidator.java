package com.copypoint.api.domain.twilioconfiguration.validation.impl;

import com.copypoint.api.domain.messagingproviderconfiguration.MessagingProviderConfiguration;
import com.copypoint.api.domain.twilioconfiguration.exceptions.TwilioConfigurationException;
import com.copypoint.api.domain.twilioconfiguration.validation.TwilioConfigurationValidator;
import org.springframework.stereotype.Component;

@Component
public class TwilioConfigurationIsValidValidator implements TwilioConfigurationValidator {
    @Override
    public void valid(MessagingProviderConfiguration config) throws TwilioConfigurationException {
        if (!config.isConfigurationValid()) {
            throw new TwilioConfigurationException("Invalid Twilio configuration");
        }
    }
}
