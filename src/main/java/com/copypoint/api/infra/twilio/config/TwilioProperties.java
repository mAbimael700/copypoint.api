package com.copypoint.api.infra.twilio.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "twilio")
@Data
public class TwilioProperties {

    private Webhook webhook = new Webhook();
    private Retry retry = new Retry();
    private Timeout timeout = new Timeout();

    @Data
    public static class Webhook {
        @Value("{app.domain.url}")
        private String baseUrl;
    }

    @Data
    public static class Retry {
        private int maxAttempts = 3;
        private int delaySeconds = 2;
    }

    @Data
    public static class Timeout {
        private int connectionSeconds = 30;
        private int readSeconds = 60;
    }
}
