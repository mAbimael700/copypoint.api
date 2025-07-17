package com.copypoint.api.infra.twilio.service;

import com.copypoint.api.domain.messaging.MessagingProviderConfig;
import com.copypoint.api.domain.messaging.MessagingProviderType;
import com.copypoint.api.domain.messaging.exceptions.MessagingException;
import com.copypoint.api.domain.messaging.service.MessagingService;
import com.copypoint.api.domain.twilioconfiguration.TwilioConfiguration;
import com.copypoint.api.domain.twilioconfiguration.validation.service.TwilioConfigurationValidationService;
import com.copypoint.api.infra.security.service.CredentialEncryptionService;
import com.copypoint.api.infra.twilio.config.TwilioProperties;
import com.copypoint.api.infra.twilio.factory.TwilioMessageFactory;
import com.twilio.Twilio;
import com.twilio.exception.TwilioException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


@Service
@RequiredArgsConstructor
@Slf4j
public class TwilioMessagingService implements MessagingService {

    @Autowired
    private TwilioProperties twilioProperties;

    @Autowired
    private TwilioSdkService sdkService;

    @Autowired
    private TwilioMessageFactory messageFactory;

    @Autowired
    private TwilioConfigurationValidationService validationService;

    @Autowired
    private CredentialEncryptionService encryptionService;

    @Override
    public boolean sendMessage(String to, String message, MessagingProviderConfig config) {

        try {
            validationService.validateConfiguration(config);

            TwilioConfiguration twilioConfiguration = (TwilioConfiguration) config;

            sdkService.initializeTwilio(twilioConfiguration);

            MessageCreator messageCreator = messageFactory.createMessage(to,
                    message,
                    twilioConfiguration);

            Message twilioMessage = messageCreator.create();

            log.info("Message sent successfully via Twilio. SID: {}, Status: {}",
                    twilioMessage.getSid(), twilioMessage.getStatus());

            return getMessageStatus(twilioMessage);

        } catch (TwilioException e) {
            log.error("Twilio error sending message to {}: {}", to, e.getMessage());
            throw new MessagingException("Failed to send message via Twilio: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error sending message via Twilio to {}", to, e);
            throw new MessagingException("Unexpected error sending message via Twilio", e);
        }
    }


    @Override
    public boolean sendMediaMessage(String to,
                                    String mediaUrl,
                                    String caption,
                                    MessagingProviderConfig config) {
        try {
            validationService.validateConfiguration(config);

            TwilioConfiguration twilioConfiguration = (TwilioConfiguration) config;

            sdkService.initializeTwilio(twilioConfiguration);

            MessageCreator messageCreator = messageFactory.createMediaMessage(
                    to,
                    mediaUrl,
                    caption,
                    twilioConfiguration
            );

            Message twilioMessage = messageCreator.create();

            log.info("Media message sent successfully via Twilio. SID: {}, Status: {}",
                    twilioMessage.getSid(), twilioMessage.getStatus());

            return getMessageStatus(twilioMessage);

        } catch (TwilioException e) {
            log.error("Twilio error sending media message to {}: {}", to, e.getMessage());
            throw new MessagingException("Failed to send media message via Twilio: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error sending media message via Twilio to {}", to, e);
            throw new MessagingException("Unexpected error sending media message via Twilio", e);
        }
    }

    @Override
    public void configureWebhook(MessagingProviderConfig config) {

        try {
            validationService.validateConfiguration(config);
            TwilioConfiguration twilioConfiguration = (TwilioConfiguration) config;

            sdkService.initializeTwilio(twilioConfiguration);

            // Configurar webhook para mensajes entrantes
            String webhookUrl = buildWebhookUrl("/webhook/twilio/incoming");

            // Actualizar la configuración del webhook en la base de datos
            twilioConfiguration.setWebhookUrl(webhookUrl);
            twilioConfiguration.setStatusCallbackUrl(buildWebhookUrl("/webhook/twilio/status"));

            log.info("Webhook configured for Twilio. URL: {}", webhookUrl);

        } catch (Exception e) {
            log.error("Error configuring Twilio webhook", e);
            throw new MessagingException("Failed to configure Twilio webhook", e);
        }
    }

    @Override
    public boolean validateConfiguration(MessagingProviderConfig config) {
        if (!(config instanceof TwilioConfiguration twilioConfiguration)) {
            return false;
        }

        try {
            String accountSid = encryptionService.decryptCredential(twilioConfiguration.getEncryptedAccountSid());
            String authToken = encryptionService.decryptCredential(twilioConfiguration.getEncryptedAuthToken());

            // Validar que las credenciales no estén vacías
            if (!StringUtils.hasText(accountSid) || !StringUtils.hasText(authToken)) {
                return false;
            }

            // Intentar inicializar Twilio para validar credenciales
            Twilio.init(accountSid, authToken);

            // Opcional: hacer una llamada de prueba a la API de Twilio
            // Account account = Account.fetcher().fetch();
            // return account != null;

            return true;

        } catch (Exception e) {
            log.error("Invalid Twilio configuration", e);
            return false;
        }
    }

    @Override
    public MessagingProviderType getSupportedProviderType() {
        return MessagingProviderType.TWILIO;
    }


    private String buildWebhookUrl(String path) {
        return twilioProperties.getWebhook().getBaseUrl() + path;
    }

    private boolean getMessageStatus(Message twilioMessage) {
        return Message.Status.ACCEPTED.equals(twilioMessage.getStatus()) ||
                Message.Status.QUEUED.equals(twilioMessage.getStatus()) ||
                Message.Status.SENDING.equals(twilioMessage.getStatus());
    }

}
