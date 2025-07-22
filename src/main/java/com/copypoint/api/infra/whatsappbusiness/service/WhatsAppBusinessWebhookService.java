package com.copypoint.api.infra.whatsappbusiness.service;

import com.copypoint.api.domain.contact.Contact;
import com.copypoint.api.domain.contact.service.ContactService;
import com.copypoint.api.domain.conversation.Conversation;
import com.copypoint.api.domain.conversation.service.ConversationService;
import com.copypoint.api.domain.customerservicephone.CustomerServicePhone;
import com.copypoint.api.domain.customerservicephone.service.CustomerServicePhoneService;
import com.copypoint.api.domain.message.Message;
import com.copypoint.api.domain.message.MessageDirection;
import com.copypoint.api.domain.message.MessageStatus;
import com.copypoint.api.domain.message.service.MessageService;
import com.copypoint.api.domain.messagingproviderconfiguration.MessagingProviderConfiguration;
import com.copypoint.api.domain.whatsappbussinessconfiguration.WhatsAppBusinessConfiguration;
import com.copypoint.api.infra.whatsappbusiness.client.WhatsAppBusinessClient;
import com.copypoint.api.infra.whatsappbusiness.dto.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class WhatsAppBusinessWebhookService {
    private static final Logger logger = LoggerFactory.getLogger(WhatsAppBusinessWebhookService.class);

    @Autowired
    private CustomerServicePhoneService customerServicePhoneService;

    @Autowired
    private ContactService contactService;

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private WhatsAppBusinessClient whatsAppClient;

    // Luego modificar el método verifyWebhookToken en WhatsAppBusinessWebhookService:
    public boolean verifyWebhookToken(Long customerServicePhoneId, String verifyToken) {
        try {
            Optional<CustomerServicePhone> phoneOpt = customerServicePhoneService.getByIdWithMessagingConfig(customerServicePhoneId);

            if (phoneOpt.isEmpty()) {
                logger.debug("No se encontró el teléfono con ID: {}", customerServicePhoneId);
                return false;
            }

            CustomerServicePhone phone = phoneOpt.get();

            MessagingProviderConfiguration config = phone.getMessagingConfig();
            if (config == null) {
                logger.debug("No hay configuración de mensajería para el teléfono: {}", customerServicePhoneId);
                return false;
            }

            if (!(config instanceof WhatsAppBusinessConfiguration)) {
                logger.debug("La configuración no es de tipo WhatsApp Business para el teléfono: {}", customerServicePhoneId);
                return false;
            }

            WhatsAppBusinessConfiguration whatsAppConfig = (WhatsAppBusinessConfiguration) config;
            boolean isValid = whatsAppConfig.getWebhookVerifyToken().equals(verifyToken);

            logger.debug("Validación de token para teléfono {}: esperado={}, recibido={}, válido={}",
                    customerServicePhoneId, whatsAppConfig.getWebhookVerifyToken(), verifyToken, isValid);

            return isValid;

        } catch (Exception e) {
            logger.error("Error verificando token de webhook para teléfono {}: {}", customerServicePhoneId, e.getMessage(), e);
            return false;
        }
    }

    public void processIncomingMessage(Long customerServicePhoneId, WhatsAppWebhookDTO webhookData, String signature) {

        Optional<CustomerServicePhone> phoneOpt = customerServicePhoneService.getByIdWithMessagingConfig(customerServicePhoneId);

        if (phoneOpt.isEmpty()) {
            throw new IllegalArgumentException("No se encontró el teléfono con ID: " + customerServicePhoneId);
        }

        CustomerServicePhone phone = phoneOpt.get();

        if (!(phone.getMessagingConfig() instanceof WhatsAppBusinessConfiguration config)) {
            throw new IllegalArgumentException("La configuración no es de tipo WhatsApp Business para el teléfono: " + customerServicePhoneId);
        }

        // Verificar firma del webhook (opcional pero recomendado para seguridad)
        if (signature != null && !verifyWebhookSignature(webhookData, signature, config.getAppSecretEncrypted())) {
            throw new SecurityException("Firma de webhook inválida");
        }

        for (WhatsAppEntryDTO entry : webhookData.entry()) {
            for (WhatsAppChangeDTO change : entry.changes()) {
                WhatsAppValueDTO value = change.value();

                // Procesar mensajes recibidos
                if (value.messages() != null) {
                    for (WhatsAppMessageDTO messageDto : value.messages()) {
                        processIncomingMessageDto(phone, messageDto, value.contacts());
                    }
                }

                // Procesar actualizaciones de estado
                if (value.statuses() != null) {
                    for (WhatsAppStatusDTO statusDto : value.statuses()) {
                        processMessageStatus(statusDto);
                    }
                }
            }
        }
    }

    private void processIncomingMessageDto(CustomerServicePhone phone, WhatsAppMessageDTO messageDto, List<WhatsAppContactDTO> contacts) {
        try {
            // Buscar o crear contacto
            Contact contact = findOrCreateContact(messageDto.from(), contacts);

            // Buscar o crear conversación
            Conversation conversation = conversationService.findOrCreateConversation(contact, phone);

            // Preparar URLs de media si existen
            List<String> mediaUrls = new ArrayList<>();
            if (messageDto.image() != null) {
                mediaUrls.add(downloadAndStoreMedia(messageDto.image().id(), phone));
            }
            if (messageDto.video() != null) {
                mediaUrls.add(downloadAndStoreMedia(messageDto.video().id(), phone));
            }
            if (messageDto.audio() != null) {
                mediaUrls.add(downloadAndStoreMedia(messageDto.audio().id(), phone));
            }
            if (messageDto.document() != null) {
                mediaUrls.add(downloadAndStoreMedia(messageDto.document().id(), phone));
            }

            // Crear mensaje
            Message message = Message.builder()
                    .messageSid(messageDto.id())
                    .direction(MessageDirection.INBOUND)
                    .status(MessageStatus.RECEIVED)
                    .conversation(conversation)
                    .mediaUrls(mediaUrls)
                    .dateSent(messageDto.getTimestampAsInstant() != null ?
                            LocalDateTime.ofInstant(messageDto.getTimestampAsInstant(), ZoneId.systemDefault()) : null)
                    .build();

            // Guardar el mensaje
            messageService.save(message);

            logger.info("Mensaje procesado exitosamente: {} de {}", messageDto.id(), messageDto.from());

        } catch (Exception e) {
            logger.error("Error procesando mensaje entrante: {}", e.getMessage(), e);
        }
    }

    private Contact findOrCreateContact(String phoneNumber, List<WhatsAppContactDTO> contacts) {
        Contact existingContact = contactService.findByPhoneNumber(phoneNumber);

        if (existingContact != null) {
            return existingContact;
        }

        // Crear nuevo contacto
        String contactName = phoneNumber; // Por defecto usar el número
        if (contacts != null && !contacts.isEmpty()) {
            WhatsAppContactDTO contactDto = contacts.stream()
                    .filter(c -> phoneNumber.equals(c.waId()))
                    .findFirst()
                    .orElse(null);

            if (contactDto != null && contactDto.profile() != null && contactDto.profile().name() != null) {
                contactName = contactDto.profile().name();
            }
        }

        Contact newContact = Contact.builder()
                .phoneNumber(phoneNumber)
                .displayName(contactName)
                .build();

        return contactService.save(newContact);
    }

    private void processMessageStatus(WhatsAppStatusDTO statusDto) {
        try {
            Message message = messageService.findByMessageSid(statusDto.id());
            if (message != null) {
                switch (statusDto.status().toLowerCase()) {
                    case "sent":
                        message.setStatus(MessageStatus.SENT);
                        break;
                    case "delivered":
                        message.setStatus(MessageStatus.DELIVERED);
                        message.setDateDelivered(LocalDateTime.now());
                        break;
                    case "read":
                        message.setStatus(MessageStatus.READ);
                        message.setDateRead(LocalDateTime.now());
                        break;
                    case "failed":
                        message.setStatus(MessageStatus.FAILED);
                        break;
                }
                messageService.save(message);
            }
        } catch (Exception e) {
            logger.error("Error actualizando estado del mensaje: {}", e.getMessage(), e);
        }
    }

    private String downloadAndStoreMedia(String mediaId, CustomerServicePhone phone) {
        try {
            if (!(phone.getMessagingConfig() instanceof WhatsAppBusinessConfiguration config)) {
                return null;
            }

            // Descargar media
            byte[] mediaData = whatsAppClient.downloadMedia(mediaId, config.getAccessTokenEncrypted());

            // Aquí deberías implementar el almacenamiento del archivo (S3, filesystem, etc.)
            // y retornar la URL donde se guardó
            // Por ahora retornamos un placeholder
            return String.format("/media/whatsapp/%s", mediaId);

        } catch (Exception e) {
            logger.error("Error descargando media {}: {}", mediaId, e.getMessage(), e);
            return null;
        }
    }

    private boolean verifyWebhookSignature(WhatsAppWebhookDTO webhookData, String signature, String encryptedAppSecret) {
        try {
            // Implementar verificación de firma webhook usando el app secret
            // Esta implementación es opcional pero recomendada para seguridad
            return true; // Placeholder
        } catch (Exception e) {
            logger.error("Error verificando firma de webhook: {}", e.getMessage(), e);
            return false;
        }
    }
}
