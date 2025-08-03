package com.copypoint.api.domain.mercadopagoconfiguration.service;

import com.copypoint.api.application.copypointintegration.exception.CopypointNotFoundException;
import com.copypoint.api.domain.copypoint.Copypoint;
import com.copypoint.api.domain.copypoint.repository.CopypointRepository;
import com.copypoint.api.domain.mercadopagoconfiguration.MercadoPagoConfiguration;
import com.copypoint.api.domain.mercadopagoconfiguration.dto.MercadoPagoConfigurationCreationDTO;
import com.copypoint.api.domain.mercadopagoconfiguration.repository.MercadoPagoConfigurationRepository;
import com.copypoint.api.domain.sale.Sale;
import com.copypoint.api.infra.security.service.CredentialEncryptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MercadoPagoConfigurationService {

    private static final Logger logger = LoggerFactory.getLogger(MercadoPagoConfigurationService.class);

    @Autowired
    private CopypointRepository copypointRepository;

    @Autowired
    private MercadoPagoConfigurationRepository mercadoPagoConfigRepository;

    @Autowired
    private CredentialEncryptionService encryptionService;

    /**
     * Obtiene la configuración de MercadoPago para una sale específica
     * Prioridad: Copypoint > Store
     */
    public Optional<MercadoPagoConfiguration> getConfigForSale(Sale sale) {
        Long copypointId = sale.getCopypoint().getId();
        return mercadoPagoConfigRepository.findActiveByCopypointId(copypointId);
    }


    public List<MercadoPagoConfiguration> getByCopypoint(Long copypointId){
        Optional<Copypoint> copypointOpt = copypointRepository.findById(copypointId);

        if (copypointOpt.isEmpty()){
            throw new CopypointNotFoundException("Copypoint not found");
        }

        Copypoint copypoint = copypointOpt.get();

        return copypoint.getMercadoPagoConfigurations();
    }


    /**
     * Obtiene el email del vendedor para la sale
     */
    public String getVendorEmailForSale(Sale sale) {
        Optional<MercadoPagoConfiguration> configOpt = getConfigForSale(sale);

        if (configOpt.isPresent()) {
            MercadoPagoConfiguration config = configOpt.get();
            return config.getVendorEmail();
        }

        // Fallback al email del usuario vendedor
        return sale.getUserVendor().getEmail();
    }

    /**
     * Obtiene el webhook secret para validar notificaciones
     */
    public String getWebhookSecretForSale(Sale sale) {
        Optional<MercadoPagoConfiguration> configOpt = getConfigForSale(sale);

        if (configOpt.isPresent()) {
            MercadoPagoConfiguration config = configOpt.get();
            return getDecryptedWebhookSecret(config);
        }

        return null;
    }


    /**
     * Guarda una nueva configuración con credenciales encriptadas
     */
    @Transactional
    public MercadoPagoConfiguration saveConfiguration(Long copypointId, MercadoPagoConfigurationCreationDTO creationDTO) {
        try {
            Optional<Copypoint> copypointOpt = copypointRepository.findById(copypointId);

            if (copypointOpt.isEmpty()) throw new RuntimeException("Copypoint con id no encontrado");

            Copypoint copypoint = copypointOpt.get();

            // Encriptar credenciales sensibles
            String encryptedAccessToken = encryptionService.encryptCredential(creationDTO.accessToken());
            String encryptedPublicKey = encryptionService.encryptCredential(creationDTO.publicKey());
            String encryptedClientSecret = encryptionService.encryptCredential(creationDTO.clientSecret());
            String encryptedWebhookSecret = creationDTO.webhookSecret() != null ?
                    encryptionService.encryptCredential(creationDTO.webhookSecret()) : null;

            // Crear la configuración
            MercadoPagoConfiguration config = MercadoPagoConfiguration.builder()
                    .copypoint(copypoint)
                    .accessTokenEncrypted(encryptedAccessToken)
                    .publicKeyEncrypted(encryptedPublicKey)
                    .clientId(creationDTO.clientId()) // No encriptado
                    .clientSecretEncrypted(encryptedClientSecret)
                    .webhookSecretEncrypted(encryptedWebhookSecret)
                    .isSandbox(creationDTO.isSandbox())
                    .isActive(true)
                    .vendorEmail(creationDTO.vendorEmail())
                    .build();

            MercadoPagoConfiguration savedConfig = mercadoPagoConfigRepository.save(config);

            logger.info("Configuración de MercadoPago guardada exitosamente para copypoint: {}",
                    copypoint.getId());

            return savedConfig;

        } catch (Exception e) {
            logger.error("Error al guardar configuración de MercadoPago: {}", e.getMessage());
            throw new RuntimeException("Error al guardar configuración de MercadoPago", e);
        }
    }

    /**
     * Desactiva una configuración
     */
    @Transactional
    public void deactivateConfiguration(Long configId) {
        Optional<MercadoPagoConfiguration> configOpt = mercadoPagoConfigRepository.findById(configId);

        if (configOpt.isPresent()) {
            MercadoPagoConfiguration config = configOpt.get();
            config.setIsActive(false);
            mercadoPagoConfigRepository.save(config);

            logger.info("Configuración de MercadoPago desactivada, ID: {}", configId);
        } else {
            throw new IllegalArgumentException("Configuración no encontrada con ID: " + configId);
        }
    }


    /**
     * Verifica si las credenciales proporcionadas coinciden con las almacenadas
     */
    public boolean verifyCredentials(Sale sale, String accessToken, String publicKey) {
        Optional<MercadoPagoConfiguration> configOpt = getConfigForSale(sale);

        if (configOpt.isPresent()) {
            MercadoPagoConfiguration config = configOpt.get();

            String storedAccessToken = getDecryptedAccessToken(config);
            String storedPublicKey = getDecryptedPublicKey(config);

            return accessToken.equals(storedAccessToken) && publicKey.equals(storedPublicKey);
        }

        return false;
    }

    /**
     * Obtiene el access token desencriptado
     */
    private String getDecryptedAccessToken(MercadoPagoConfiguration config) {
        try {
            return encryptionService.decryptCredential(config.getAccessTokenEncrypted());
        } catch (Exception e) {
            logger.error("Error al desencriptar access token: {}", e.getMessage());
            return null;
        }
    }


    /**
     * Obtiene el webhook secret desencriptado
     */
    private String getDecryptedWebhookSecret(MercadoPagoConfiguration config) {
        try {
            return encryptionService.decryptCredential(config.getWebhookSecretEncrypted());
        } catch (Exception e) {
            logger.error("Error al desencriptar webhook secret: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Obtiene el public key desencriptado
     */
    private String getDecryptedPublicKey(MercadoPagoConfiguration config) {
        try {
            return encryptionService.decryptCredential(config.getPublicKeyEncrypted());
        } catch (Exception e) {
            logger.error("Error al desencriptar public key: {}", e.getMessage());
            return null;
        }
    }
}
