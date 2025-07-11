package com.copypoint.api.infra.security.utils;

import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class AESUtil {
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String KEY_ALGORITHM = "AES";
    private static final int IV_LENGTH = 16;
    private static final int KEY_LENGTH = 256;

    /**
     * Encripta un texto usando AES-256-CBC
     * @param plainText Texto a encriptar
     * @param secretKey Clave secreta (debe ser de 32 caracteres para AES-256)
     * @return Texto encriptado en Base64 (IV + datos encriptados)
     */
    public String encrypt(String plainText, String secretKey) {
        try {
            if (plainText == null || plainText.isEmpty()) {
                throw new IllegalArgumentException("El texto a encriptar no puede estar vacío");
            }

            if (secretKey == null || secretKey.length() != 32) {
                throw new IllegalArgumentException("La clave secreta debe tener exactamente 32 caracteres");
            }

            // Crear la clave secreta
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), KEY_ALGORITHM);

            // Crear el cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);

            // Generar IV aleatorio
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            // Inicializar cipher para encriptación
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

            // Encriptar el texto
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Concatenar IV + datos encriptados
            byte[] encryptedWithIv = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, encryptedWithIv, 0, iv.length);
            System.arraycopy(encrypted, 0, encryptedWithIv, iv.length, encrypted.length);

            // Codificar en Base64
            return Base64.getEncoder().encodeToString(encryptedWithIv);

        } catch (Exception e) {
            throw new RuntimeException("Error al encriptar: " + e.getMessage(), e);
        }
    }

    /**
     * Desencripta un texto usando AES-256-CBC
     * @param encryptedText Texto encriptado en Base64
     * @param secretKey Clave secreta (debe ser de 32 caracteres para AES-256)
     * @return Texto desencriptado
     */
    public String decrypt(String encryptedText, String secretKey) {
        try {
            if (encryptedText == null || encryptedText.isEmpty()) {
                throw new IllegalArgumentException("El texto encriptado no puede estar vacío");
            }

            if (secretKey == null || secretKey.length() != 32) {
                throw new IllegalArgumentException("La clave secreta debe tener exactamente 32 caracteres");
            }

            // Decodificar de Base64
            byte[] encryptedWithIv = Base64.getDecoder().decode(encryptedText);

            if (encryptedWithIv.length < IV_LENGTH) {
                throw new IllegalArgumentException("Datos encriptados inválidos");
            }

            // Extraer IV y datos encriptados
            byte[] iv = new byte[IV_LENGTH];
            byte[] encrypted = new byte[encryptedWithIv.length - IV_LENGTH];
            System.arraycopy(encryptedWithIv, 0, iv, 0, IV_LENGTH);
            System.arraycopy(encryptedWithIv, IV_LENGTH, encrypted, 0, encrypted.length);

            // Crear la clave secreta
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), KEY_ALGORITHM);

            // Crear el cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            // Inicializar cipher para desencriptación
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            // Desencriptar los datos
            byte[] decrypted = cipher.doFinal(encrypted);

            return new String(decrypted, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException("Error al desencriptar: " + e.getMessage(), e);
        }
    }

    /**
     * Genera una clave secreta aleatoria de 32 caracteres para AES-256
     * @return Clave secreta de 32 caracteres
     */
    public String generateSecretKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(KEY_ALGORITHM);
            keyGen.init(KEY_LENGTH);
            SecretKey secretKey = keyGen.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded()).substring(0, 32);
        } catch (Exception e) {
            throw new RuntimeException("Error al generar clave secreta: " + e.getMessage(), e);
        }
    }

    /**
     * Valida que una clave secreta tenga el formato correcto
     * @param secretKey Clave a validar
     * @return true si la clave es válida
     */
    public boolean isValidSecretKey(String secretKey) {
        return secretKey != null && secretKey.length() == 32;
    }
}
