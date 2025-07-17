package com.copypoint.api.domain.twilioconfiguration.validation;

import com.copypoint.api.domain.messagingproviderconfiguration.MessagingProviderConfiguration;
import com.copypoint.api.domain.twilioconfiguration.exceptions.TwilioConfigurationException;

public interface TwilioConfigurationValidator {
    void valid(MessagingProviderConfiguration config) throws TwilioConfigurationException;
}
