package com.copypoint.api.domain.attachment.service;

import com.copypoint.api.domain.attachment.Attachment;
import com.copypoint.api.domain.attachment.repository.AttachmentRepository;
import com.copypoint.api.infra.cloudflare.r2.service.CloudflareR2Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service

public class AttachmentMediaService {

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private CloudflareR2Service cloudflareR2Service;

    /**
     * Obtiene un attachment por ID desde la base de datos
     */
    public Optional<Attachment> findById(Long id) {
        return attachmentRepository.findById(id);
    }

    /**
     * Descarga el archivo del attachment desde R2 y lo retorna como ResponseEntity
     */
    public ResponseEntity<byte[]> downloadAttachmentFile(Long attachmentId) {
        // Buscar el attachment en la base de datos
        Optional<Attachment> attachmentOpt = attachmentRepository.findById(attachmentId);

        if (attachmentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Attachment attachment = attachmentOpt.get();

        // Verificar que el archivo esté descargado y tenga una ruta de almacenamiento
        if (!attachment.isDownloaded() || attachment.getStoragePath() == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            // Descargar el archivo desde R2
            byte[] fileContent = cloudflareR2Service.downloadFile(attachment.getStoragePath());

            // Determinar el MediaType basado en el mimeType o fileType
            MediaType mediaType = determineMediaType(attachment);

            // Construir los headers de respuesta
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(mediaType);
            headers.setContentLength(fileContent.length);

            // Opcional: agregar el nombre original del archivo
            if (attachment.getOriginalName() != null) {
                headers.add(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + attachment.getOriginalName() + "\"");
            }

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileContent);

        } catch (Exception e) {
            // Log del error (aquí puedes usar tu logger preferido)
            System.err.println("Error descargando archivo con ID " + attachmentId + ": " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Genera una URL prefirmada para acceso directo al archivo
     */
    public Optional<String> generatePresignedUrl(Long attachmentId, int expirationMinutes) {
        Optional<Attachment> attachmentOpt = attachmentRepository.findById(attachmentId);

        if (attachmentOpt.isEmpty()) {
            return Optional.empty();
        }

        Attachment attachment = attachmentOpt.get();

        if (!attachment.isDownloaded() || attachment.getStoragePath() == null) {
            return Optional.empty();
        }

        try {
            String presignedUrl = cloudflareR2Service.generatePresignedUrl(
                    attachment.getStoragePath(),
                    expirationMinutes
            );
            return Optional.of(presignedUrl);
        } catch (Exception e) {
            System.err.println("Error generando URL prefirmada para attachment ID " + attachmentId + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Verifica si un attachment existe y está disponible
     */
    public boolean isAttachmentAvailable(Long attachmentId) {
        Optional<Attachment> attachmentOpt = attachmentRepository.findById(attachmentId);

        if (attachmentOpt.isEmpty()) {
            return false;
        }

        Attachment attachment = attachmentOpt.get();

        // Verificar que esté descargado y tenga ruta de almacenamiento
        if (!attachment.isDownloaded() || attachment.getStoragePath() == null) {
            return false;
        }

        // Verificar que el archivo exista en R2
        try {
            return cloudflareR2Service.fileExists(attachment.getStoragePath());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Determina el MediaType basado en el mimeType o fileType del attachment
     */
    private MediaType determineMediaType(Attachment attachment) {
        // Primero intentar usar el mimeType si está disponible
        if (attachment.getMimeType() != null && !attachment.getMimeType().isEmpty()) {
            try {
                return MediaType.parseMediaType(attachment.getMimeType());
            } catch (Exception e) {
                // Si no se puede parsear, continuar con la lógica por fileType
            }
        }

        // Fallback basado en fileType
        if (attachment.getFileType() != null) {
            switch (attachment.getFileType()) {
                case PDF:
                    return MediaType.APPLICATION_PDF;
                case PNG:
                    return MediaType.IMAGE_PNG;
                case JPG:
                    return MediaType.IMAGE_JPEG;
                case IMAGE:
                    return MediaType.IMAGE_JPEG; // Default para imágenes
                case DOC:
                case DOCX:
                    return MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
                case TXT:
                    return MediaType.TEXT_PLAIN;
                case VIDEO:
                    return MediaType.parseMediaType("video/mp4"); // Default para videos
                case AUDIO:
                    return MediaType.parseMediaType("audio/mpeg"); // Default para audio
                default:
                    return MediaType.APPLICATION_OCTET_STREAM;
            }
        }

        return MediaType.APPLICATION_OCTET_STREAM;
    }
}
