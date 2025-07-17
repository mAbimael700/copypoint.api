    package com.copypoint.api.domain.contact;

    import com.copypoint.api.domain.client.Client;
    import com.copypoint.api.domain.conversation.Conversation;
    import com.copypoint.api.domain.user.User;
    import jakarta.persistence.*;
    import lombok.AllArgsConstructor;
    import lombok.Builder;
    import lombok.Data;
    import lombok.NoArgsConstructor;

    import java.time.LocalDateTime;
    import java.util.ArrayList;
    import java.util.List;

    @Entity
    @Table(name = "contacts")
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public class Contact {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumns({
                @JoinColumn(name = "client_id", referencedColumnName = "client_id"),
                @JoinColumn(name = "store_id", referencedColumnName = "store_id")
        })
        private Client clientInfo;

        @Column(name = "phone_number") // <- Buena práctica
        private String phoneNumber;

        @Column(name = "display_name") // <- Buena práctica
        private String displayName;

        @Column(name = "profile_photo") // <- Buena práctica
        private String profilePhoto;

        @Column(name = "last_activity") // <- Buena práctica
        private LocalDateTime lastActivity;

        @Enumerated(EnumType.STRING) // <- Para el enum
        private ContactStatus status;

        @Builder.Default
        @OneToMany(mappedBy = "customerContact", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
        private List<Conversation> conversations = new ArrayList<>();

    }
