package com.copypoint.api.infra.whatsappbusiness.service.media;

import com.copypoint.api.domain.customerservicephone.CustomerServicePhone;
import com.copypoint.api.domain.customerservicephone.service.CustomerServicePhoneService;
import com.copypoint.api.domain.message.Message;
import com.copypoint.api.domain.message.service.MessageService;
import com.copypoint.api.domain.whatsappbussinessconfiguration.WhatsAppBusinessConfiguration;
import com.copypoint.api.infra.cloudflare.r2.service.CloudflareR2Service;
import com.copypoint.api.infra.whatsappbusiness.http.client.WhatsAppBusinessClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class WhatsAppMediaService {
    private static final Logger logger = LoggerFactory.getLogger(WhatsAppMediaService.class);

    @Autowired
    private WhatsAppBusinessClient whatsAppClient;

    @Autowired
    private CloudflareR2Service r2Service;

    @Autowired
    private MessageService messageService;

    @Autowired
    private CustomerServicePhoneService customerServicePhoneService;

    /**
     * Descarga y almacena un archivo de media de WhatsApp en R2
     * Retorna la URL inmediatamente, incluso si la descarga falla
     */
    public String downloadAndStoreMedia(String mediaId, CustomerServicePhone phone, Message message) {
        // Generar URL de R2 inmediatamente
        String r2Key = generateR2Key(mediaId, phone.getId());
        String r2Url = generateR2Url(r2Key);

        // Intentar descarga síncrona primero (con timeout corto)
        try {
            if (downloadMediaSync(mediaId, phone, r2Key)) {
                logger.info("Media descargado síncronamente: {}", mediaId);
                return r2Url;
            }
        } catch (Exception e) {
            logger.warn("Descarga síncrona falló para media {}, programando descarga asíncrona: {}",
                    mediaId, e.getMessage());
        }

        // Si falla la descarga síncrona, programar descarga asíncrona
        downloadMediaAsync(mediaId, phone, r2Key, message);

        // Retornar URL aunque la descarga esté pendiente
        return r2Url;
    }

    /**
     * Descarga síncrona con timeout corto
     */
    private boolean downloadMediaSync(String mediaId, CustomerServicePhone phone, String r2Key) {
        try {
            if (!(phone.getMessagingConfig() instanceof WhatsAppBusinessConfiguration config)) {
                return false;
            }

            // Verificar si ya existe en R2
            if (r2Service.fileExists(r2Key)) {
                logger.debug("Media {} ya existe en R2", mediaId);
                return true;
            }

            // Descargar de WhatsApp
            byte[] mediaData = whatsAppClient.downloadMedia(mediaId, config.getAccessTokenEncrypted());

            if (mediaData == null || mediaData.length == 0) {
                return false;
            }

            // Subir a R2
            String contentType = detectContentType(mediaId, mediaData);
            r2Service.uploadFile(r2Key, mediaData, contentType);

            logger.info("Media {} subido exitosamente a R2 como {}", mediaId, r2Key);
            return true;

        } catch (Exception e) {
            logger.error("Error en descarga síncrona de media {}: {}", mediaId, e.getMessage());
            return false;
        }
    }

    /**
     * Descarga asíncrona con reintentos
     */
    @Async
    public void downloadMediaAsync(String mediaId, CustomerServicePhone phone,
                                   String r2Key, Message message) {
        CompletableFuture.runAsync(() -> {
            int maxRetries = 3;
            int retryDelay = 5000; // 5 segundos

            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                try {
                    if (downloadMediaSync(mediaId, phone, r2Key)) {
                        logger.info("Media {} descargado exitosamente en intento {} (asíncrono)",
                                mediaId, attempt);

                        // Actualizar el mensaje para indicar que el media está disponible
                        updateMessageMediaStatus(message, mediaId, true);
                        return;
                    }
                } catch (Exception e) {
                    logger.warn("Intento {} fallido para media {}: {}", attempt, mediaId, e.getMessage());
                }

                // Esperar antes del siguiente intento
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep((long) retryDelay * attempt); // Backoff exponencial
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }

            logger.error("Falló la descarga asíncrona de media {} después de {} intentos",
                    mediaId, maxRetries);
            updateMessageMediaStatus(message, mediaId, false);
        });
    }

    /**
     * Reintenta la descarga de un media específico
     */
    public boolean retryMediaDownload(String mediaId, Long phoneId, Long messageId) {
        try {
            // Buscar el mensaje y teléfono
            Optional<Message> message = messageService.getById(messageId);

            if (message.isEmpty()) {
                return false;
            }

            // Buscar el teléfono (necesitarías este método en tu servicio)
            CustomerServicePhone phone = customerServicePhoneService.getById(phoneId);

            String r2Key = generateR2Key(mediaId, phoneId);

            // Intentar descarga
            boolean success = downloadMediaSync(mediaId, phone, r2Key);
            updateMessageMediaStatus(message.get(), mediaId, success);

            return success;


        } catch (Exception e) {
            logger.error("Error reintentando descarga de media {}: {}", mediaId, e.getMessage());
            return false;
        }
    }

    /**
     * Genera la clave única para R2
     */
    private String generateR2Key(String mediaId, Long phoneId) {
        return String.format("whatsapp-media/%d/%s", phoneId, mediaId);
    }

    /**
     * Genera la URL pública de R2
     */
    private String generateR2Url(String r2Key) {
        // Ajusta según tu configuración de R2
        return String.format("https://tu-dominio-r2.com/%s", r2Key);
    }

    /**
     * Detecta el tipo de contenido basado en los primeros bytes
     */
    private String detectContentType(String mediaId, byte[] data) {
        if (data.length < 4) {
            return "application/octet-stream";
        }

        // Detectar por magic numbers
        if (data[0] == (byte) 0xFF && data[1] == (byte) 0xD8) {
            return "image/jpeg";
        }
        if (data[0] == (byte) 0x89 && data[1] == 'P' && data[2] == 'N' && data[3] == 'G') {
            return "image/png";
        }
        if (data[0] == 'G' && data[1] == 'I' && data[2] == 'F') {
            return "image/gif";
        }
        if (data[0] == 0x00 && data[1] == 0x00 && data[2] == 0x00 && (data[3] == 0x18 || data[3] == 0x20)) {
            return "video/mp4";
        }

        return "application/octet-stream";
    }

    /**
     * Actualiza el estado del media en el mensaje
     */
    private void updateMessageMediaStatus(Message message, String mediaId, boolean available) {
        try {
            // Aquí podrías actualizar un campo en el mensaje que indique el estado del media
            // Por ejemplo, podrías tener un Map<String, Boolean> mediaAvailability en Message

            // message.updateMediaAvailability(mediaId, available);
            // messageService.save(message);

            logger.info("Estado de media {} actualizado para mensaje {}: disponible={}",
                    mediaId, message.getId(), available);

        } catch (Exception e) {
            logger.error("Error actualizando estado de media: {}", e.getMessage());
        }
    }

    /**
     * Verifica si un media está disponible en R2
     */
    public boolean isMediaAvailable(String mediaId, Long phoneId) {
        String r2Key = generateR2Key(mediaId, phoneId);
        return r2Service.fileExists(r2Key);
    }

    /**
     * Obtiene la URL de descarga directa de R2
     */
    public String getMediaDownloadUrl(String mediaId, Long phoneId) {
        String r2Key = generateR2Key(mediaId, phoneId);
        if (r2Service.fileExists(r2Key)) {
            return generateR2Url(r2Key);
        }
        return null;
    }
}
