package com.copypoint.api.domain.twilioconfiguration.validation.impl;

import com.copypoint.api.domain.messaging.MessagingProviderConfig;
import com.copypoint.api.domain.twilioconfiguration.TwilioConfiguration;
import com.copypoint.api.domain.twilioconfiguration.exceptions.TwilioConfigurationException;
import com.copypoint.api.domain.twilioconfiguration.validation.TwilioConfigurationValidator;

public class TwilioConfigurationInstanceValidator implements TwilioConfigurationValidator {
    @Override
    public void valid(MessagingProviderConfig config) throws TwilioConfigurationException {
        if (!(config instanceof TwilioConfiguration twilioConfiguration)) {
            throw new TwilioConfigurationException("Invalid configuration type for Twilio service");
        }
    }
}
