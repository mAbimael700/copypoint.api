package com.copypoint.api.domain.message;

import com.copypoint.api.domain.conversation.Conversation;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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


}
