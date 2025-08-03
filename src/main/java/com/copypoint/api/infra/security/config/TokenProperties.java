package com.copypoint.api.infra.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.security.token")
public class TokenProperties {
    private String secret;
    private int defaultLength = 32;
    private int webhookLength = 64;
    private boolean enableLogging = true;

    // Getters y Setters
    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public int getDefaultLength() {
        return defaultLength;
    }

    public void setDefaultLength(int defaultLength) {
        this.defaultLength = defaultLength;
    }

    public int getWebhookLength() {
        return webhookLength;
    }

    public void setWebhookLength(int webhookLength) {
        this.webhookLength = webhookLength;
    }

    public boolean isEnableLogging() {
        return enableLogging;
    }

    public void setEnableLogging(boolean enableLogging) {
        this.enableLogging = enableLogging;
    }
}
