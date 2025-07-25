package com.copypoint.api.infra.whatsappbusiness.service.webhook;

import com.copypoint.api.domain.attachment.Attachment;
import com.copypoint.api.domain.attachment.AttachmentFileType;
import com.copypoint.api.domain.attachment.service.AttachmentService;
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
import com.copypoint.api.infra.whatsappbusiness.http.client.WhatsAppBusinessClient;
import com.copypoint.api.infra.whatsappbusiness.dto.response.*;
import com.copypoint.api.infra.whatsappbusiness.service.media.WhatsAppMediaService;
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

    @Autowired
    private WhatsAppMediaService whatsAppMediaService;

    @Autowired
    private AttachmentService attachmentService;

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

            if (!(config instanceof WhatsAppBusinessConfiguration whatsAppConfig)) {
                logger.debug("La configuración no es de tipo WhatsApp Business para el teléfono: {}", customerServicePhoneId);
                return false;
            }

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

            String bodyText = "";

            if (messageDto.text() != null) {
                bodyText = messageDto.text().body();
            }

            // Crear el mensaje PRIMERO sin las URLs de media
            Message message = Message.builder()
                    .messageSid(messageDto.id())
                    .direction(MessageDirection.INBOUND)
                    .status(MessageStatus.RECEIVED)
                    .conversation(conversation)
                    .body(bodyText)
                    .mediaUrls(new ArrayList<>()) // Inicializar vacío
                    .dateSent(messageDto.getTimestampAsInstant() != null ?
                            LocalDateTime.ofInstant(messageDto.getTimestampAsInstant(), ZoneId.systemDefault()) : null)
                    .build();

            // Guardar el mensaje para obtener su ID
            message = messageService.save(message);


            try {
                if (messageDto.image() != null) {
                    String originalName = messageDto.image().caption();
                    if (originalName == null) {
                        originalName = "image_" + messageDto.image().id();
                    }

                    Attachment attachment = whatsAppMediaService.downloadAndStoreMedia(
                            messageDto.image().id(), phone, message,
                            originalName, AttachmentFileType.IMAGE);

                    // Si hay caption en la imagen, usarla como texto
                    if (messageDto.image().caption() != null && bodyText.isEmpty()) {
                        bodyText = messageDto.image().caption();
                    }
                }

                if (messageDto.video() != null) {
                    String originalName = messageDto.video().caption();
                    if (originalName == null) {
                        originalName = "video_" + messageDto.video().id();
                    }

                    Attachment attachment = whatsAppMediaService.downloadAndStoreMedia(
                            messageDto.video().id(), phone, message,
                            originalName, AttachmentFileType.VIDEO);

                    // Si hay caption en el video, usarla como texto
                    if (messageDto.video().caption() != null && bodyText.isEmpty()) {
                        bodyText = messageDto.video().caption();
                    }
                }

                if (messageDto.audio() != null) {
                    String originalName = messageDto.audio().caption();
                    if (originalName == null) {
                        originalName = "audio_" + messageDto.audio().id();
                    }

                    Attachment attachment = whatsAppMediaService.downloadAndStoreMedia(
                            messageDto.audio().id(), phone, message,
                            originalName, AttachmentFileType.AUDIO);
                }

                if (messageDto.document() != null) {
                    String originalName = messageDto.audio().caption();

                    if (originalName == null) {
                        originalName = "audio_" + messageDto.audio().id();
                    }

                    Attachment attachment = whatsAppMediaService.downloadAndStoreMedia(
                            messageDto.audio().id(), phone, message,
                            originalName, AttachmentFileType.AUDIO);

                }

                if (messageDto.document() != null) {
                    String originalName = messageDto.document().filename();
                    if (originalName == null) {
                        originalName = "document_" + messageDto.document().id();
                    }

                    // Detectar tipo de archivo basado en el nombre
                    AttachmentFileType fileType = attachmentService.detectFileType(
                            originalName, messageDto.document().mimeType());

                    Attachment attachment = whatsAppMediaService.downloadAndStoreMedia(
                            messageDto.document().id(), phone, message,
                            originalName, fileType);
                }

            } catch (Exception mediaException) {
                // Log el error pero no fallar el procesamiento del mensaje
                logger.warn("Error procesando medios para mensaje {}: {}",
                        messageDto.id(), mediaException.getMessage());
            }

            // Actualizar el cuerpo del mensaje si se modificó por captions
            if (!bodyText.equals(message.getBody())) {
                message.setBody(bodyText);
                messageService.save(message);
            }

            logger.info("Mensaje procesado exitosamente: {} de {} (attachments: {})",
                    messageDto.id(), messageDto.from(), message.getAttachments().size());

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
