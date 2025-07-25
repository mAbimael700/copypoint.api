package com.copypoint.api.domain.attachment;

import com.copypoint.api.domain.message.Message;
import com.copypoint.api.domain.saleprofile.SaleProfile;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "attachments")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Attachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nuevos campos para manejo de medios de WhatsApp
    @Column(name = "media_sid", length = 100)
    private String mediaSid; // ID del media en WhatsApp

    // ✅ ESTO ESTÁ BIEN
    @OneToMany(mappedBy = "attachment", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<SaleProfile> saleProfiles = new ArrayList<>();  // <- Lista, no objeto singular

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id")
    private Message message;

    @Column(name = "original_name")
    private String originalName;

    @Column(name = "storage_path", length = 500)
    private String storagePath;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", length = 50)
    private AttachmentFileType fileType;

    private Integer pages;

    private Integer copies;

    @Column(name = "uploaded_by", length = 20)
    private String uploadedBy;

    private Boolean active;

    @Enumerated(EnumType.STRING)
    @Column(name = "download_status")
    @Builder.Default
    private AttachmentDownloadStatus downloadStatus = AttachmentDownloadStatus.PENDING;

    @Column(name = "download_attempts")
    @Builder.Default
    private Integer downloadAttempts = 0;

    @Column(name = "last_download_attempt")
    private LocalDateTime lastDownloadAttempt;

    @Column(name = "download_error_message", length = 500)
    private String downloadErrorMessage;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    // Fechas de auditoría
    @Column(name = "date_created", nullable = false)
    @Builder.Default
    private LocalDateTime dateCreated = LocalDateTime.now();

    @Column(name = "date_updated")
    private LocalDateTime dateUpdated;

    @Column(name = "date_downloaded")
    private LocalDateTime dateDownloaded;

    // Métodos de conveniencia
    public boolean isDownloaded() {
        return downloadStatus == AttachmentDownloadStatus.DOWNLOADED;
    }

    public boolean isPending() {
        return downloadStatus == AttachmentDownloadStatus.PENDING;
    }

    public boolean isFailed() {
        return downloadStatus == AttachmentDownloadStatus.FAILED;
    }

    public void markAsDownloading() {
        this.downloadStatus = AttachmentDownloadStatus.DOWNLOADING;
        this.lastDownloadAttempt = LocalDateTime.now();
        this.downloadAttempts++;
    }

    public void markAsDownloaded() {
        this.downloadStatus = AttachmentDownloadStatus.DOWNLOADED;
        this.dateDownloaded = LocalDateTime.now();
        this.downloadErrorMessage = null;
    }

    public void markAsFailed(String errorMessage) {
        this.downloadStatus = AttachmentDownloadStatus.FAILED;
        this.downloadErrorMessage = errorMessage;
    }

    // Métodos de auditoría
    @PreUpdate
    protected void onUpdate() {
        dateUpdated = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (dateCreated == null) {
            dateCreated = LocalDateTime.now();
        }
    }


}
