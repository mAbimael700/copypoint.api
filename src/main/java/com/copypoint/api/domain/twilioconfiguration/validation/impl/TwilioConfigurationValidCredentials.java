package com.copypoint.api.domain.twilioconfiguration.validation.impl;

import com.copypoint.api.domain.messagingproviderconfiguration.MessagingProviderConfiguration;
import com.copypoint.api.domain.twilioconfiguration.TwilioConfiguration;
import com.copypoint.api.domain.twilioconfiguration.exceptions.TwilioConfigurationException;
import com.copypoint.api.domain.twilioconfiguration.validation.TwilioConfigurationValidator;
import com.copypoint.api.infra.security.service.CredentialEncryptionService;
import com.twilio.Twilio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class TwilioConfigurationValidCredentials implements TwilioConfigurationValidator {

    @Autowired
    private CredentialEncryptionService encryptionService;

    @Override
    public void valid(MessagingProviderConfiguration config) throws TwilioConfigurationException {
        try {
            TwilioConfiguration twilioConfiguration = (TwilioConfiguration) config;

            String accountSid = encryptionService.decryptCredential(twilioConfiguration.getEncryptedAccountSid());
            String authToken = encryptionService.decryptCredential(twilioConfiguration.getEncryptedAuthToken());

            // Validar que las credenciales no estén vacías
            if (!StringUtils.hasText(accountSid) || !StringUtils.hasText(authToken)) {
                throw new TwilioConfigurationException("Credenciales están vacías");
            }

            // Intentar inicializar Twilio para validar credenciales
            Twilio.init(accountSid, authToken);

            // Opcional: hacer una llamada de prueba a la API de Twilio
            // Account account = Account.fetcher().fetch();
            // return account != null;

        } catch (Exception e) {
            throw new TwilioConfigurationException("Invalid Twilio configuration " + e.getMessage());
        }
    }
}
