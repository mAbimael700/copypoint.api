package com.copypoint.api.domain.conversation;

import com.copypoint.api.domain.contact.Contact;
import com.copypoint.api.domain.customerservicephone.CustomerServicePhone;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversations")
@Data
@NoArgsConstructor
@AllArgsConstructor
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
}
