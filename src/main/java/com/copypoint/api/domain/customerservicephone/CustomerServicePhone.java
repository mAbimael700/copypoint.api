package com.copypoint.api.domain.customerservicephone;

import com.copypoint.api.domain.conversation.Conversation;
import com.copypoint.api.domain.copypoint.Copypoint;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customer_service_phones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerServicePhone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @JoinColumn(name = "copypoint_id",referencedColumnName = "id")
    private Copypoint copypoint;

    @Builder.Default
    @OneToMany(mappedBy = "customerServicePhone", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Conversation> conversations = new ArrayList<>();

    private String phoneNumber;
}
