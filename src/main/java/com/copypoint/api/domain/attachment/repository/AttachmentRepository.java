package com.copypoint.api.domain.attachment.repository;

import com.copypoint.api.domain.attachment.Attachment;
import com.copypoint.api.domain.attachment.AttachmentDownloadStatus;
import com.copypoint.api.domain.message.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findByMessage(Message message);

    List<Attachment> findByMediaSid(String mediaSid);

    List<Attachment> findByDownloadStatus(AttachmentDownloadStatus attachmentDownloadStatus);

    @Query("SELECT a FROM Attachment a WHERE a.downloadStatus = 'FAILED' AND a.downloadAttempts < :maxAttempts")
    List<Attachment> findFailedAttachmentsForRetry(@Param("maxAttempts") int maxAttempts);

    @Query("SELECT a FROM Attachment a WHERE a.downloadStatus = 'PENDING' OR " +
            "(a.downloadStatus = 'DOWNLOADING' AND a.lastDownloadAttempt < :timeoutThreshold)")
    List<Attachment> findStuckDownloads(@Param("timeoutThreshold") LocalDateTime timeoutThreshold);

    List<Attachment> findByMessageAndDownloadStatus(Message message, AttachmentDownloadStatus status);

    @Query("SELECT COUNT(a) FROM Attachment a WHERE a.message = :message AND a.downloadStatus = 'DOWNLOADED'")
    long countDownloadedByMessage(@Param("message") Message message);

    @Query("SELECT COUNT(a) FROM Attachment a WHERE a.message = :message")
    long countByMessage(@Param("message") Message message);
}
