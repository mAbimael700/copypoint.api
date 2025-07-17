package com.copypoint.api.domain.twilioconfiguration.validation;

import com.copypoint.api.domain.messaging.MessagingProviderConfig;
import com.copypoint.api.domain.twilioconfiguration.exceptions.TwilioConfigurationException;

public interface TwilioConfigurationValidator {
    void valid(MessagingProviderConfig config) throws TwilioConfigurationException;
}
