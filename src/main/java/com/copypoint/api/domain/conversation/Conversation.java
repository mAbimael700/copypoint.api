package com.copypoint.api.domain.conversation;

import com.copypoint.api.domain.contact.Contact;
import com.copypoint.api.domain.customerservicephone.CustomerServicePhone;
import com.copypoint.api.domain.message.Message;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "conversations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_contact_id")
    private Contact customerContact;

    @ManyToOne
    @JoinColumn(name = "customer_service_phone_id")
    private CustomerServicePhone customerServicePhone;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;

    @OneToMany(mappedBy = "conversation", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Message> messages = new ArrayList<>();
}
