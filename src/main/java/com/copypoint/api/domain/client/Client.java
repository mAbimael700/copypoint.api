package com.copypoint.api.domain.client;

import com.copypoint.api.domain.contact.Contact;
import com.copypoint.api.domain.store.Store;
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
@Table(name = "clients")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {

    @EmbeddedId
    private ClientId id;

/*    @Column(name = "client_id", insertable = false, updatable = false)
    private Long clientId;

    @Column(name = "store_id", insertable = false, updatable = false)
    private Long storeId;*/

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("clientId")
    @JoinColumn(name = "client_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("storeId")
    @JoinColumn(name = "store_id", referencedColumnName = "id")
    private Store store;

    @Enumerated(EnumType.STRING)
    @Column(name = "client_status")
    private ClientStatus clientStatus; // ACTIVE, INACTIVE, BLOCKED, etc.

    @Column(name = "registered_at")
    private LocalDateTime registeredAt;

    // Relación con contacts
    @OneToMany(mappedBy = "clientInfo", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<Contact> contacts = new ArrayList<>();
}
