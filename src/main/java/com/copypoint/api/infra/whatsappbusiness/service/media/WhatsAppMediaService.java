package com.copypoint.api.infra.whatsappbusiness.service.media;

import com.copypoint.api.domain.attachment.Attachment;
import com.copypoint.api.domain.attachment.AttachmentDownloadStatus;
import com.copypoint.api.domain.attachment.AttachmentFileType;
import com.copypoint.api.domain.attachment.service.AttachmentService;
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

import java.util.List;
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

    @Autowired
    private AttachmentService attachmentService;

    /**
     * Crea un attachment y descarga el media de WhatsApp
     * Retorna el attachment inmediatamente, incluso si la descarga está pendiente
     */
    public Attachment downloadAndStoreMedia(String mediaId,
                                            CustomerServicePhone phone,
                                            Message message,
                                            String originalName,
                                            AttachmentFileType fileType
    ) {

        Attachment attachment = attachmentService.createWhatsAppAttachment(
                message, mediaId, originalName, fileType);

        attachment = attachmentService.save(attachment);

        // Asociar el attachment al mensaje
        message.addAttachment(attachment);

        // Generar URL de R2 inmediatamente
        String r2Key = generateR2Key(mediaId, phone.getId());

        // Intentar descarga síncrona primero (con timeout corto)
        try {
            if (downloadMediaSync(mediaId, phone, r2Key, attachment)) {
                logger.info("Media descargado síncronamente: {}", mediaId);
                return attachment;
                ;
            }
        } catch (Exception e) {
            logger.warn("Descarga síncrona falló para media {}, programando descarga asíncrona: {}",
                    mediaId, e.getMessage());
        }

        // Si falla la descarga síncrona, programar descarga asíncrona
        downloadMediaAsync(mediaId, phone, r2Key, message);

        // Retornar URL aunque la descarga esté pendiente
        return attachment;
    }

    /**
     * Descarga síncrona con timeout corto
     */
    private boolean downloadMediaSync(String mediaId, CustomerServicePhone phone,
                                      String r2Key, Attachment attachment) {
        try {
            if (!(phone.getMessagingConfig() instanceof WhatsAppBusinessConfiguration config)) {
                return false;
            }

            // Verificar si ya existe en R2
            if (r2Service.fileExists(r2Key)) {
                logger.debug("Media {} ya existe en R2", mediaId);
                updateAttachmentAsDownloaded(attachment, r2Key);
                return true;
            }

            // Marcar como descargando
            attachmentService.markAsDownloading(attachment);

            // Descargar de WhatsApp
            byte[] mediaData = whatsAppClient.downloadMedia(mediaId, config.getAccessTokenEncrypted());

            if (mediaData == null || mediaData.length == 0) {
                return false;
            }

            // Detectar tipo de contenido
            String contentType = detectContentType(mediaId, mediaData);

            // Actualizar el attachment con información del archivo
            attachment.setMimeType(contentType);
            attachment.setFileSizeBytes((long) mediaData.length);


            // Actualizar tipo de archivo si no fue especificado
            if (attachment.getFileType() == AttachmentFileType.OTHER) {
                AttachmentFileType detectedType = attachmentService.detectFileType(
                        attachment.getOriginalName(), contentType);
                attachment.setFileType(detectedType);
            }

            // Subir a R2
            r2Service.uploadFile(r2Key, mediaData, contentType);

            // Marcar como descargado exitosamente
            attachmentService.markAsDownloaded(attachment,
                    r2Key,
                    contentType,
                    (long) mediaData.length);

            logger.info("Media {} subido exitosamente a R2 como {}", mediaId, r2Key);
            return true;

        } catch (Exception e) {
            logger.error("Error en descarga síncrona de media {}: {}", mediaId, e.getMessage());

            // Marcar attachment como fallido
            attachmentService.markAsFailed(attachment, "Error descarga síncrona: " + e.getMessage());

            return false;
        }
    }

    /**
     * Descarga asíncrona con reintentos
     */
    @Async
    public void downloadMediaAsync(String mediaId, CustomerServicePhone phone,
                                   String r2Key, Attachment attachment) {
        CompletableFuture.runAsync(() -> {
            int maxRetries = 3;
            int retryDelay = 5000; // 5 segundos

            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                try {
                    if (downloadMediaSync(mediaId, phone, r2Key, attachment)) {
                        logger.info("Media {} descargado exitosamente en intento {} (asíncrono)",
                                mediaId, attempt);
                        return;
                    }
                } catch (Exception e) {
                    logger.warn("Intento {} fallido para media {}: {}", attempt, mediaId, e.getMessage());

                    // Actualizar el número de intentos en el attachment
                    attachment.setDownloadAttempts(attempt);
                    attachmentService.save(attachment);
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

            // Marcar como fallido definitivamente
            attachmentService.markAsFailed(attachment,
                    "Falló después de " + maxRetries + " intentos de descarga");
        });
    }

    /**
     * Reintenta la descarga de un media específico
     */
    public boolean retryMediaDownload(Long attachmentId) {
        try {
            Optional<Attachment> attachmentOpt = attachmentService.findById(attachmentId);

            if (attachmentOpt.isEmpty()) {
                logger.warn("No se encontró attachment con ID: {}", attachmentId);
                return false;
            }

            Attachment attachment = attachmentOpt.get();
            String mediaId = attachment.getMediaSid();

            if (mediaId == null) {
                logger.warn("Attachment {} no tiene mediaSid", attachmentId);
                return false;
            }

            Message message = attachment.getMessage();
            if (message == null) {
                logger.warn("Attachment {} no tiene mensaje asociado", attachmentId);
                return false;
            }

            // Obtener el teléfono de customer service desde la conversación
            CustomerServicePhone phone = message.getConversation().getCustomerServicePhone();

            String r2Key = generateR2Key(mediaId, phone.getId());

            // Resetear estado para reintento
            attachment.setDownloadStatus(AttachmentDownloadStatus.PENDING);
            attachment.setDownloadErrorMessage(null);
            attachmentService.save(attachment);

            // Intentar descarga
            boolean success = downloadMediaSync(mediaId, phone, r2Key, attachment);

            if (!success) {
                // Si falla síncronamente, programar descarga asíncrona
                downloadMediaAsync(mediaId, phone, r2Key, attachment);
            }

            return success;

        } catch (Exception e) {
            logger.error("Error reintentando descarga de attachment {}: {}", attachmentId, e.getMessage());
            return false;
        }
    }

    /*
     * Método para reintentar descargas fallidas masivamente
     */
    public void retryFailedDownloads(int maxAttempts) {
        try {
            List<Attachment> failedAttachments = attachmentService.findAttachmentsForRetry(maxAttempts);

            logger.info("Reintentando {} descargas fallidas", failedAttachments.size());

            for (Attachment attachment : failedAttachments) {
                try {
                    retryMediaDownload(attachment.getId());
                } catch (Exception e) {
                    logger.error("Error reintentando descarga de attachment {}: {}",
                            attachment.getId(), e.getMessage());
                }
            }

        } catch (Exception e) {
            logger.error("Error en proceso de reintento masivo: {}", e.getMessage());
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
     * Actualiza un attachment como descargado cuando ya existe en R2
     */
    private void updateAttachmentAsDownloaded(Attachment attachment, String r2Key) {
        try {
            // Generar URL de descarga
            String downloadUrl = generateR2Url(r2Key);

            // Marcar como descargado
            attachmentService.markAsDownloaded(attachment, r2Key,
                    attachment.getMimeType(), attachment.getFileSizeBytes());

            logger.debug("Attachment {} marcado como descargado (ya existía en R2)",
                    attachment.getId());

        } catch (Exception e) {
            logger.error("Error actualizando attachment como descargado: {}", e.getMessage());
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
     * Obtiene la URL de descarga directa de R2 para un attachment
     */
    public String getMediaDownloadUrl(Attachment attachment) {
        if (attachment == null || !attachment.isDownloaded()) {
            return null;
        }

        String r2Key = attachment.getStoragePath();
        if (r2Key != null && r2Service.fileExists(r2Key)) {
            return generateR2Url(r2Key);
        }

        return null;
    }

    /**
     * Obtiene la URL de descarga por mediaId y phoneId (método de compatibilidad)
     */
    public String getMediaDownloadUrl(String mediaId, Long phoneId) {
        String r2Key = generateR2Key(mediaId, phoneId);
        if (r2Service.fileExists(r2Key)) {
            return generateR2Url(r2Key);
        }
        return null;
    }

    /**
     * Obtiene estadísticas de descargas para un mensaje
     */
    public AttachmentDownloadStats getDownloadStats(Message message) {
        if (!message.hasAttachments()) {
            return new AttachmentDownloadStats(0, 0, 0, 0);
        }

        long total = message.getAttachments().size();
        long downloaded = message.getAttachments().stream()
                .mapToLong(a -> a.isDownloaded() ? 1 : 0).sum();
        long pending = message.getAttachments().stream()
                .mapToLong(a -> a.isPending() ? 1 : 0).sum();
        long failed = message.getAttachments().stream()
                .mapToLong(a -> a.isFailed() ? 1 : 0).sum();

        return new AttachmentDownloadStats(total, downloaded, pending, failed);
    }

    /**
     * Clase para estadísticas de descarga
     */
    public record AttachmentDownloadStats(long total,
                                          long downloaded,
                                          long pending,
                                          long failed) {

        public boolean isComplete() {
            return downloaded == total;
        }

        public boolean hasFailures() {
            return failed > 0;
        }

        public double getSuccessRate() {
            return total > 0 ? (double) downloaded / total : 0.0;
        }
    }
}
