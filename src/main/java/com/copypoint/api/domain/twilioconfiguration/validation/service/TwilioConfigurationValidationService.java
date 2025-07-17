package com.copypoint.api.domain.twilioconfiguration.validation.service;


import com.copypoint.api.domain.messaging.MessagingProviderConfig;
import com.copypoint.api.domain.twilioconfiguration.validation.TwilioConfigurationValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TwilioConfigurationValidationService {

    @Autowired
    private List<TwilioConfigurationValidator> validators;

    /**
     * Ejecuta todas las validaciones registradas
     */
    public void validateConfiguration(MessagingProviderConfig config) {

        for (TwilioConfigurationValidator validator : validators) {
            validator.valid(config);
        }
    }
}
