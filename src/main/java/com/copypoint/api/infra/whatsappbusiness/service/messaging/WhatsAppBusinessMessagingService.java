package com.copypoint.api.infra.whatsappbusiness.service.messaging;

import com.copypoint.api.domain.messagingproviderconfiguration.MessagingProviderConfiguration;
import com.copypoint.api.domain.messagingproviderconfiguration.MessagingProviderType;
import com.copypoint.api.domain.messagingproviderconfiguration.service.MessagingService;
import com.copypoint.api.domain.whatsappbussinessconfiguration.WhatsAppBusinessConfiguration;
import com.copypoint.api.infra.whatsappbusiness.dto.response.WhatsAppSendMessageRequestDTO;
import com.copypoint.api.infra.whatsappbusiness.dto.response.WhatsAppSendMessageResponseDTO;
import com.copypoint.api.infra.whatsappbusiness.http.client.WhatsAppBusinessClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WhatsAppBusinessMessagingService implements MessagingService {
    private static final Logger logger = LoggerFactory.getLogger(WhatsAppBusinessMessagingService.class);

    @Autowired
    private WhatsAppBusinessClient whatsAppClient;

    @Value("${app.domain.url}")
    private String domainUrl;

    @Override
    public boolean sendMessage(String to, String message, MessagingProviderConfiguration config) {
        if (!(config instanceof WhatsAppBusinessConfiguration whatsAppConfig)) {
            throw new IllegalArgumentException("Configuración inválida para WhatsApp Business");
        }

        try {
            WhatsAppSendMessageRequestDTO request = WhatsAppSendMessageRequestDTO.createTextMessage(to, message);
            WhatsAppSendMessageResponseDTO response = whatsAppClient.sendMessage(
                    request,
                    whatsAppConfig.getPhoneNumberId(),
                    whatsAppConfig.getAccessTokenEncrypted()
            );

            return response != null && response.messages() != null && !response.messages().isEmpty();
        } catch (Exception e) {
            logger.error("Error enviando mensaje de WhatsApp: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean sendMediaMessage(String to, String mediaUrl, String caption, MessagingProviderConfiguration config) {
        if (!(config instanceof WhatsAppBusinessConfiguration whatsAppConfig)) {
            throw new IllegalArgumentException("Configuración inválida para WhatsApp Business");
        }

        try {
            // Para WhatsApp Business API necesitamos primero subir el media y obtener su ID
            // Esto requiere implementación adicional para subir archivos
            WhatsAppSendMessageRequestDTO request = WhatsAppSendMessageRequestDTO.createImageMessage(to, mediaUrl, caption);
            WhatsAppSendMessageResponseDTO response = whatsAppClient.sendMessage(
                    request,
                    whatsAppConfig.getPhoneNumberId(),
                    whatsAppConfig.getAccessTokenEncrypted()
            );

            return response != null && response.messages() != null && !response.messages().isEmpty();
        } catch (Exception e) {
            logger.error("Error enviando mensaje multimedia de WhatsApp: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public void configureWebhook(MessagingProviderConfiguration config) {
        // Para WhatsApp Business API, los webhooks se configuran a nivel de aplicación en el Meta Business Manager
        // No se pueden configurar programáticamente por teléfono individual
        logger.info("Los webhooks de WhatsApp Business API se configuran en Meta Business Manager");
    }

    @Override
    public boolean validateConfiguration(MessagingProviderConfiguration config) {
        if (!(config instanceof WhatsAppBusinessConfiguration whatsAppConfig)) {
            return false;
        }

        return whatsAppConfig.isConfigurationValid();
    }

    @Override
    public MessagingProviderType getSupportedProviderType() {
        return MessagingProviderType.WHATSAPP_BUSINESS_API;
    }

    public String getWebhookUrl(Long customerServicePhoneId) {
        return String.format("%s/api/webhook/whatsapp/%s", domainUrl, customerServicePhoneId);
    }

}
