package com.copypoint.api.domain.copypoint;

import com.copypoint.api.domain.customerservicephone.CustomerServicePhone;
import com.copypoint.api.domain.mercadopagoconfiguration.MercadoPagoConfiguration;
import com.copypoint.api.domain.store.Store;
import com.copypoint.api.domain.user.User;
import com.copypoint.api.domain.employee.Employee;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "copypoints")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Copypoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "store_id")
    private Store store;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_modified_at")
    private LocalDateTime lastModifiedAt;

    @ManyToOne
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsible_id")
    private User responsible;

    @Column(unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    private CopypointStatus status;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "copypoint")
    @Builder.Default
    private List<MercadoPagoConfiguration> mercadoPagoConfigurations = new ArrayList<>();

    // Integración de mensajería - WhatsApp Business, Twilio
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "copypoint")
    @Builder.Default
    private List<CustomerServicePhone> customerServicePhones = new ArrayList<>();

    // Método helper para obtener todas las integraciones de mensajería activas
    public List<CustomerServicePhone> getActiveMessagingIntegrations() {
        return customerServicePhones.stream()
                .filter(CustomerServicePhone::hasValidConfiguration)
                .toList();
    }

    // Método helper para obtener todas las integraciones de pago activas
    public List<MercadoPagoConfiguration> getActivePaymentIntegrations() {
        return mercadoPagoConfigurations.stream()
                .filter(config -> config.getIsActive() != null && config.getIsActive())
                .toList();
    }

    // Método helper para verificar si tiene integraciones activas
    public boolean hasActiveIntegrations() {
        return !getActivePaymentIntegrations().isEmpty() ||
                !getActiveMessagingIntegrations().isEmpty();
    }
}
