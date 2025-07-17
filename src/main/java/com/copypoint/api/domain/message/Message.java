package com.copypoint.api.domain.message;

import com.copypoint.api.domain.conversation.Conversation;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "messages")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_sid")
    private String messageSid;

    @Enumerated(EnumType.STRING)
    private MessageDirection direction;

    @Enumerated(EnumType.STRING)
    private MessageStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    @ElementCollection
    @CollectionTable(
            name = "message_attachment_urls",
            joinColumns = @JoinColumn(name = "message_id")
    )
    @Column(name = "attachment_url", length = 500)
    @Builder.Default
    private List<String> mediaUrls = new ArrayList<>();

    // Fecha cuando el mensaje fue enviado desde Twilio (fecha oficial del mensaje)
    @Column(name = "date_sent")
    private LocalDateTime dateSent;

    // Fecha cuando el mensaje fue entregado al destinatario
    @Column(name = "date_delivered")
    private LocalDateTime dateDelivered;

    // Fecha cuando el mensaje fue leído por el destinatario
    @Column(name = "date_read")
    private LocalDateTime dateRead;

    // Fecha cuando el mensaje fue creado en nuestro sistema
    @Column(name = "date_created", nullable = false)
    @Builder.Default
    private LocalDateTime dateCreated = LocalDateTime.now();

    // Fecha de la última actualización del mensaje
    @Column(name = "date_updated")
    private LocalDateTime dateUpdated;

    // Método para actualizar la fecha de modificación automáticamente
    @PreUpdate
    protected void onUpdate() {
        dateUpdated = LocalDateTime.now();
    }

    // Método para establecer la fecha de creación automáticamente
    @PrePersist
    protected void onCreate() {
        if (dateCreated == null) {
            dateCreated = LocalDateTime.now();
        }
    }

}
