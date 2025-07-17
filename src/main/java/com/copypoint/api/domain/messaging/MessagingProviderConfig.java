package com.copypoint.api.domain.messaging;

import com.copypoint.api.domain.customerservicephone.CustomerServicePhone;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "messaging_provider_configs")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "provider_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class MessagingProviderConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "messagingConfig", fetch = FetchType.LAZY)
    private CustomerServicePhone customerServicePhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider_type", insertable = false, updatable = false)
    private MessagingProviderType providerType;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "display_name")
    private String displayName;

    // Método abstracto para validar configuración
    public abstract boolean isConfigurationValid();
}
