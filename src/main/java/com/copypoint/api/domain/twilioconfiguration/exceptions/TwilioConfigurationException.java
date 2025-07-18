package com.copypoint.api.domain.twilioconfiguration.exceptions;

import com.copypoint.api.domain.messagingproviderconfiguration.exceptions.MessagingException;

public class TwilioConfigurationException extends MessagingException {
    public TwilioConfigurationException(String message) {
        super(message);
    }
}
