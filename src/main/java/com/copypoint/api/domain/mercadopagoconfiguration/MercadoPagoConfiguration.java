package com.copypoint.api.domain.mercadopagoconfiguration;

import com.copypoint.api.domain.copypoint.Copypoint;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "mercadopago_configs")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MercadoPagoConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "copypoint_id", referencedColumnName = "id")
    private Copypoint copypoint;

    @Column(name = "access_token", length = 500)
    private String accessToken;

    @Column(name = "public_key", length = 500)
    private String publicKey;

    @Column(name = "client_id", length = 200)
    private String clientId;

    @Column(name = "client_secret", length = 500)
    private String clientSecret;

    @Column(name = "webhook_secret", length = 200)
    private String webhookSecret;

    @Column(name = "is_sandbox")
    private Boolean isSandbox;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "vendor_email", length = 200)
    private String vendorEmail;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
