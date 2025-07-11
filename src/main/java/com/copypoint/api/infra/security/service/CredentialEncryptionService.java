package com.copypoint.api.infra.security.service;

import com.copypoint.api.infra.security.utils.AESUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CredentialEncryptionService {
    private static final Logger logger = LoggerFactory.getLogger(CredentialEncryptionService.class);

    @Autowired
    private AESUtil aesUtil;

    @Value("${app.encryption.secret-key}")
    private String secretKey;

    /**
     * Encripta una credencial sensible
     * @param rawCredential Credencial en texto plano
     * @return Credencial encriptada en Base64
     */
    public String encryptCredential(String rawCredential) {
        if (rawCredential == null || rawCredential.trim().isEmpty()) {
            throw new IllegalArgumentException("La credencial no puede estar vacía");
        }

        try {
            validateSecretKey();
            String encrypted = aesUtil.encrypt(rawCredential.trim(), secretKey);
            logger.debug("Credencial encriptada exitosamente");
            return encrypted;
        } catch (Exception e) {
            logger.error("Error al encriptar credencial: {}", e.getMessage());
            throw new RuntimeException("Error al encriptar credencial", e);
        }
    }

    /**
     * Desencripta una credencial
     * @param encryptedCredential Credencial encriptada en Base64
     * @return Credencial en texto plano
     */
    public String decryptCredential(String encryptedCredential) {
        if (encryptedCredential == null || encryptedCredential.trim().isEmpty()) {
            throw new IllegalArgumentException("La credencial encriptada no puede estar vacía");
        }

        try {
            validateSecretKey();
            String decrypted = aesUtil.decrypt(encryptedCredential.trim(), secretKey);
            logger.debug("Credencial desencriptada exitosamente");
            return decrypted;
        } catch (Exception e) {
            logger.error("Error al desencriptar credencial: {}", e.getMessage());
            throw new RuntimeException("Error al desencriptar credencial", e);
        }
    }

    /**
     * Verifica si una credencial en texto plano coincide con la encriptada
     * @param rawCredential Credencial en texto plano
     * @param encryptedCredential Credencial encriptada
     * @return true si coinciden
     */
    public boolean verifyCredential(String rawCredential, String encryptedCredential) {
        if (rawCredential == null || encryptedCredential == null) {
            return false;
        }

        try {
            String decrypted = decryptCredential(encryptedCredential);
            boolean matches = rawCredential.equals(decrypted);
            logger.debug("Verificación de credencial: {}", matches ? "exitosa" : "fallida");
            return matches;
        } catch (Exception e) {
            logger.error("Error al verificar credencial: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Encripta múltiples credenciales de una vez
     * @param credentials Array de credenciales en texto plano
     * @return Array de credenciales encriptadas
     */
    public String[] encryptMultipleCredentials(String... credentials) {
        if (credentials == null || credentials.length == 0) {
            throw new IllegalArgumentException("Debe proporcionar al menos una credencial");
        }

        String[] encrypted = new String[credentials.length];
        for (int i = 0; i < credentials.length; i++) {
            encrypted[i] = credentials[i] != null ? encryptCredential(credentials[i]) : null;
        }

        return encrypted;
    }

    /**
     * Desencripta múltiples credenciales de una vez
     * @param encryptedCredentials Array de credenciales encriptadas
     * @return Array de credenciales en texto plano
     */
    public String[] decryptMultipleCredentials(String... encryptedCredentials) {
        if (encryptedCredentials == null || encryptedCredentials.length == 0) {
            throw new IllegalArgumentException("Debe proporcionar al menos una credencial encriptada");
        }

        String[] decrypted = new String[encryptedCredentials.length];
        for (int i = 0; i < encryptedCredentials.length; i++) {
            decrypted[i] = encryptedCredentials[i] != null ? decryptCredential(encryptedCredentials[i]) : null;
        }

        return decrypted;
    }

    /**
     * Valida que la clave secreta esté configurada correctamente
     */
    private void validateSecretKey() {
        if (!aesUtil.isValidSecretKey(secretKey)) {
            throw new IllegalStateException("La clave secreta no está configurada correctamente. Debe tener 32 caracteres.");
        }
    }

    /**
     * Genera una nueva clave secreta (para uso en desarrollo/configuración)
     * @return Nueva clave secreta de 32 caracteres
     */
    public String generateNewSecretKey() {
        return aesUtil.generateSecretKey();
    }
}
