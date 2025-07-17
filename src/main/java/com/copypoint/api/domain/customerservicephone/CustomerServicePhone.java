package com.copypoint.api.domain.customerservicephone;

import com.copypoint.api.domain.conversation.Conversation;
import com.copypoint.api.domain.copypoint.Copypoint;
import com.copypoint.api.domain.messagingproviderconfiguration.MessagingProviderConfiguration;
import com.copypoint.api.domain.messagingproviderconfiguration.MessagingProviderType;
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

    // Relación con configuración del proveedor
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "messaging_config_id")
    private MessagingProviderConfiguration messagingConfig;

    // Método helper para obtener el tipo de proveedor
    public MessagingProviderType getProviderType() {
        return messagingConfig != null ? messagingConfig.getProviderType() : null;
    }

    // Método helper para verificar si la configuración es válida
    public boolean hasValidConfiguration() {
        return messagingConfig != null &&
                messagingConfig.getIsActive() &&
                messagingConfig.isConfigurationValid();
    }
}
