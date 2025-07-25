package com.copypoint.api.domain.attachment.service;

import com.copypoint.api.domain.attachment.Attachment;
import com.copypoint.api.domain.attachment.AttachmentDownloadStatus;
import com.copypoint.api.domain.attachment.AttachmentFileType;
import com.copypoint.api.domain.attachment.repository.AttachmentRepository;
import com.copypoint.api.domain.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AttachmentService {
    @Autowired
    private AttachmentRepository attachmentRepository;

    public Attachment save(Attachment attachment) {
        return attachmentRepository.save(attachment);
    }

    public Optional<Attachment> findById(Long id) {
        return attachmentRepository.findById(id);
    }

    public List<Attachment> findByMessage(Message message) {
        return attachmentRepository.findByMessage(message);
    }

    public List<Attachment> findByMediaSid(String mediaSid) {
        return attachmentRepository.findByMediaSid(mediaSid);
    }

    public List<Attachment> findPendingDownloads() {
        return attachmentRepository.findByDownloadStatus(AttachmentDownloadStatus.PENDING);
    }

    public List<Attachment> findFailedDownloads() {
        return attachmentRepository.findByDownloadStatus(AttachmentDownloadStatus.FAILED);
    }

    /**
     * Crea un attachment para un mensaje de WhatsApp
     */
    public Attachment createWhatsAppAttachment(Message message, String whatsappMediaId,
                                               String originalName, AttachmentFileType fileType) {
        return Attachment.builder()
                .message(message)
                .mediaSid(whatsappMediaId)
                .originalName(originalName)
                .fileType(fileType)
                .downloadStatus(AttachmentDownloadStatus.PENDING)
                .downloadAttempts(0)
                .active(true)
                .build();
    }

    /**
     * Actualiza el attachment cuando se completa la descarga
     */
    public void markAsDownloaded(Attachment attachment, String storagePath,
                                 String mimeType, Long fileSizeBytes) {
        attachment.setStoragePath(storagePath);
        attachment.setMimeType(mimeType);
        attachment.setFileSizeBytes(fileSizeBytes);
        attachment.markAsDownloaded();

        save(attachment);
    }

    /**
     * Marca un attachment como fallido
     */
    public void markAsFailed(Attachment attachment, String errorMessage) {
        attachment.markAsFailed(errorMessage);
        save(attachment);
    }

    /**
     * Inicia el proceso de descarga de un attachment
     */
    public void markAsDownloading(Attachment attachment) {
        attachment.markAsDownloading();
        save(attachment);
    }

    /**
     * Busca attachments que necesitan reintento
     */
    public List<Attachment> findAttachmentsForRetry(int maxAttempts) {
        return attachmentRepository.findFailedAttachmentsForRetry(maxAttempts);
    }

    /**
     * Detecta el tipo de archivo basado en el nombre o extensión
     */
    public AttachmentFileType detectFileType(String fileName, String mimeType) {
        if (mimeType != null) {
            if (mimeType.startsWith("image/")) return AttachmentFileType.IMAGE;
            if (mimeType.startsWith("video/")) return AttachmentFileType.VIDEO;
            if (mimeType.startsWith("audio/")) return AttachmentFileType.AUDIO;
            if (mimeType.equals("application/pdf")) return AttachmentFileType.PDF;
        }

        if (fileName != null) {
            String extension = getFileExtension(fileName).toLowerCase();
            return switch (extension) {
                case "jpg", "jpeg", "png", "gif", "webp" -> AttachmentFileType.IMAGE;
                case "mp4", "mov", "avi", "mkv" -> AttachmentFileType.VIDEO;
                case "mp3", "wav", "ogg", "m4a" -> AttachmentFileType.AUDIO;
                case "pdf" -> AttachmentFileType.PDF;
                case "doc", "docx" -> AttachmentFileType.DOCUMENT;
                case "xls", "xlsx" -> AttachmentFileType.SPREADSHEET;
                case "ppt", "pptx" -> AttachmentFileType.PRESENTATION;
                default -> AttachmentFileType.OTHER;
            };
        }

        return AttachmentFileType.OTHER;
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex > 0 ? fileName.substring(lastDotIndex + 1) : "";
    }
}
