package com.copypoint.api.infra.security.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.integrations")
public class IntegrationProperties {

    private WhatsAppConfig whatsapp = new WhatsAppConfig();
    private MercadoPagoConfig mercadopago = new MercadoPagoConfig();

    @Setter
    @Getter
    public static class WhatsAppConfig {
        private WebhookConfig webhook = new WebhookConfig();

    }

    @Setter
    @Getter
    public static class MercadoPagoConfig {
        private WebhookConfig webhook = new WebhookConfig();

    }

    @Setter
    @Getter
    public static class WebhookConfig {
        private String verifyToken;
        private String tokenPrefix;
        private int tokenExpiryHours = 24;

    }

    // Getters y Setters principales
    public WhatsAppConfig getWhatsapp() {
        return whatsapp;
    }

    public void setWhatsapp(WhatsAppConfig whatsapp) {
        this.whatsapp = whatsapp;
    }

    public MercadoPagoConfig getMercadopago() {
        return mercadopago;
    }

    public void setMercadopago(MercadoPagoConfig mercadopago) {
        this.mercadopago = mercadopago;
    }
}
