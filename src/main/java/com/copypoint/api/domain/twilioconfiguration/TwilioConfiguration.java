package com.copypoint.api.domain.twilioconfiguration;

import com.copypoint.api.domain.messaging.MessagingProviderConfig;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "twilio_configurations")
@DiscriminatorValue("TWILIO")
@Data
@EqualsAndHashCode
@SuperBuilder
public class TwilioConfiguration extends MessagingProviderConfig {
    @Column(name = "account_sid", nullable = false, length = 1000)
    private String encryptedAccountSid; // Encriptado con AES

    @Column(name = "auth_token", nullable = false, length = 1000)
    private String encryptedAuthToken; // Encriptado con AES

    @Column(name = "webhook_url")
    private String webhookUrl;

    @Column(name = "status_callback_url")
    private String statusCallbackUrl;

    @Override
    public boolean isConfigurationValid() {
        return encryptedAccountSid != null &&
                encryptedAuthToken != null &&
                !encryptedAccountSid.trim().isEmpty() &&
                !encryptedAuthToken.trim().isEmpty();
    }
}
