package com.copypoint.api.infra.security.utils;

import com.copypoint.api.infra.security.config.IntegrationProperties;
import com.copypoint.api.infra.security.config.TokenProperties;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Component
public class SecureTokenGenerator {
    private static final Logger logger = LoggerFactory.getLogger(SecureTokenGenerator.class);
    private static final SecureRandom secureRandom = new SecureRandom();

    // Configuraciones por defecto
    private static final int DEFAULT_TOKEN_LENGTH = 32;
    private static final int WEBHOOK_TOKEN_LENGTH = 64;
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final TokenProperties tokenProperties;

    public SecureTokenGenerator(TokenProperties tokenProperties,
                                IntegrationProperties integrationProperties) {
        this.tokenProperties = tokenProperties;
    }

    /**
     * Genera un token básico seguro con longitud personalizable
     */
    public String generateSecureToken(int length) {
        byte[] tokenBytes = new byte[length];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    /**
     * Genera un token básico seguro con longitud por defecto
     */
    public String generateSecureToken() {
        return generateSecureToken(tokenProperties.getDefaultLength());
    }

    /**
     * Genera un token para verificación de webhook de WhatsApp
     * Más largo y con mayor entropía para webhooks
     */
    public String generateWhatsAppVerifyToken() {
        if (tokenProperties.isEnableLogging()) {
            logger.info("Generando token de verificación para WhatsApp webhook");
        }
        return generateSecureToken(tokenProperties.getWebhookLength());
    }

    /**
     * Genera un token para MercadoPago con información adicional
     * Incluye timestamp y UUID para mayor singularidad
     */
    public String generateMercadoPagoToken() {
        if (tokenProperties.isEnableLogging()) {
            logger.info("Generando token para integración con MercadoPago");
        }

        // Combinar múltiples fuentes de entropía
        UUID uuid = UUID.randomUUID();
        long timestamp = Instant.now().toEpochMilli();
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);

        String baseData = uuid.toString() + "-" + timestamp + "-" +
                Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(baseData.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Genera un token HMAC firmado (si se proporciona secret)
     * Útil para tokens que necesitan verificación de integridad
     */
    public String generateSignedToken(String data) {
        String secret = tokenProperties.getSecret();
        if (secret == null || secret.trim().isEmpty()) {
            if (tokenProperties.isEnableLogging()) {
                logger.warn("No se encontró secret configurado, generando token sin firma");
            }
            return generateSecureToken();
        }

        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8),
                    HMAC_ALGORITHM
            );
            mac.init(secretKeySpec);

            // Agregar timestamp para evitar replay attacks
            String payload = data + "-" + Instant.now().toEpochMilli();
            byte[] hmacBytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            return Base64.getUrlEncoder().withoutPadding().encodeToString(hmacBytes);

        } catch (Exception e) {
            if (tokenProperties.isEnableLogging()) {
                logger.error("Error generando token firmado: {}", e.getMessage());
            }
            // Fallback a token seguro básico
            return generateSecureToken();
        }
    }

    /**
     * Genera un token con prefijo para identificar su propósito
     */
    public String generatePrefixedToken(String prefix) {
        String token = generateSecureToken();
        return prefix + "_" + token;
    }

    /**
     * Valida que un token tenga el formato esperado
     */
    public boolean isValidTokenFormat(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        try {
            // Verificar que sea Base64 URL-safe válido
            Base64.getUrlDecoder().decode(token);
            return token.length() >= 32; // Mínimo 32 caracteres
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Genera un token específico para webhooks con validación de formato
     */
    public WebhookToken generateWebhookToken(String service) {
        String token = generateSecureToken(tokenProperties.getWebhookLength());
        String prefixedToken = service.toLowerCase() + "_wh_" + token;

        return new WebhookToken(
                prefixedToken,
                Instant.now(),
                service
        );
    }

    /**
         * Clase interna para tokens de webhook con metadatos
         */
        public record WebhookToken(String token, Instant createdAt, String service) {

        @Override
            public String toString() {
                return "WebhookToken{" +
                        "service='" + service + '\'' +
                        ", createdAt=" + createdAt +
                        ", tokenLength=" + (token != null ? token.length() : 0) +
                        '}';
            }
        }
}
