package com.copypoint.api.infra.whatsappbusiness.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class WhatsAppWebhookTokenGenerator {
    private static final SecureRandom secureRandom = new SecureRandom();

    public String generateVerifyToken() {
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
}
