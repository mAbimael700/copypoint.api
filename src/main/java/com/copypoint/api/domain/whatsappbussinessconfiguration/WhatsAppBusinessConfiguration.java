package com.copypoint.api.domain.whatsappbussinessconfiguration;

import com.copypoint.api.domain.messagingproviderconfiguration.MessagingProviderConfiguration;
import com.copypoint.api.domain.messagingproviderconfiguration.MessagingProviderType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "whatsapp_business_configurations")
@DiscriminatorValue("WHATSAPP_BUSINESS_API")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class WhatsAppBusinessConfiguration extends MessagingProviderConfiguration {
    @Column(name = "access_token_encrypted", length = 500)
    private String accessTokenEncrypted;

    @Column(name = "phone_number_id", nullable = false)
    private String phoneNumberId;

    @Column(name = "business_account_id", nullable = false)
    private String businessAccountId;

    @Column(name = "webhook_verify_token", nullable = false)
    private String webhookVerifyToken;

    @Column(name = "app_id")
    private String appId;

    @Column(name = "app_secret_encrypted", length = 500)
    private String appSecretEncrypted;

    @Override
    public boolean isConfigurationValid() {
        return accessTokenEncrypted != null && !accessTokenEncrypted.trim().isEmpty() &&
                phoneNumberId != null && !phoneNumberId.trim().isEmpty() &&
                businessAccountId != null && !businessAccountId.trim().isEmpty() &&
                webhookVerifyToken != null && !webhookVerifyToken.trim().isEmpty();
    }

    @PostLoad
    @PostPersist
    @PostUpdate
    private void setProviderType() {
        super.setProviderType(MessagingProviderType.WHATSAPP_BUSINESS_API);
    }
}
