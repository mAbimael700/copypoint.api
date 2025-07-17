package com.copypoint.api.infra.twilio.service;

import com.copypoint.api.domain.twilioconfiguration.TwilioConfiguration;
import com.copypoint.api.domain.twilioconfiguration.exceptions.TwilioConfigurationException;
import com.copypoint.api.infra.security.service.CredentialEncryptionService;
import com.twilio.Twilio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TwilioSdkService {
    @Autowired
    private CredentialEncryptionService encryptionService;

    public void initializeTwilio(TwilioConfiguration config) {
        try {
            String accountSid = encryptionService.decryptCredential(config.getEncryptedAccountSid());
            String authToken = encryptionService.decryptCredential(config.getEncryptedAuthToken());

            Twilio.init(accountSid, authToken);
        } catch (Exception e) {
            throw new TwilioConfigurationException("Failed to initialize Twilio client");
        }
    }
}
