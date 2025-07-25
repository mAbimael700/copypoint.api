package com.copypoint.api.domain.attachment;

public enum AttachmentDownloadStatus {
    PENDING,        // Pendiente de descarga
    DOWNLOADING,    // En proceso de descarga
    DOWNLOADED,     // Descargado exitosamente
    FAILED,         // Falló la descarga
    UNAVAILABLE     // No disponible en el origen (expiró en WhatsApp)
}
