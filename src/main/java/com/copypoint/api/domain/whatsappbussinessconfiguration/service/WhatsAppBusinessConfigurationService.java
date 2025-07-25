package com.copypoint.api.domain.whatsappbussinessconfiguration.service;

import com.copypoint.api.domain.customerservicephone.CustomerServicePhone;
import com.copypoint.api.domain.customerservicephone.service.CustomerServicePhoneService;
import com.copypoint.api.domain.whatsappbussinessconfiguration.WhatsAppBusinessConfiguration;
import com.copypoint.api.domain.whatsappbussinessconfiguration.repository.WhatsAppBusinessConfigurationRepository;
import com.copypoint.api.infra.security.service.CredentialEncryptionService;
import com.copypoint.api.domain.whatsappbussinessconfiguration.dto.WhatsAppConfigurationDTO;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class WhatsAppBusinessConfigurationService {

    @Autowired
    private WhatsAppBusinessConfigurationRepository repository;

    @Autowired
    private CustomerServicePhoneService customerServicePhoneService;

    @Autowired
    private CredentialEncryptionService encryptionService;

    public WhatsAppBusinessConfiguration createConfiguration(Long phoneId,
                                                             WhatsAppConfigurationDTO dto) {

        CustomerServicePhone phone = customerServicePhoneService.getById(phoneId);

        if (phone == null) {
            throw new RuntimeException("No existe una configuración con ese número registrado");
        }
        // Verificar si ya existe una configuración
        if (phone.getMessagingConfig() != null) {
            throw new RuntimeException("El teléfono ya tiene una configuración de WhatsApp Business activa");
        }

        WhatsAppBusinessConfiguration config = WhatsAppBusinessConfiguration.builder()
                .accessTokenEncrypted(encryptionService.encryptCredential(dto.accessToken()))
                .phoneNumberId(dto.phoneNumberId())
                .businessAccountId(dto.businessAccountId())
                .webhookVerifyToken(dto.webhookVerifyToken())
                .appId(dto.appId())
                .appSecretEncrypted(dto.appSecret() != null ?
                        encryptionService.encryptCredential(dto.appSecret()) : null)
                .displayName(phone.getCopypoint().getName() + " - " + dto.displayName())
                .isActive(dto.isActive() != null ? dto.isActive() : true)
                .build();

        config = repository.save(config);

        // Actualizar la referencia en el teléfono
        phone.setMessagingConfig(config);

        return config;
    }

    public WhatsAppBusinessConfiguration updateConfiguration(Long configId,
                                                             WhatsAppConfigurationDTO dto) {


        Optional<WhatsAppBusinessConfiguration> configuration = repository.findById(configId); // Asumiendo que existe este método

        if (configuration.isEmpty()) {
            throw new RuntimeException("Configuration not found");
        }

        WhatsAppBusinessConfiguration existing = configuration.get();

        if (dto.accessToken() != null) {
            existing.setAccessTokenEncrypted(encryptionService.encryptCredential(dto.accessToken()));
        }
        if (dto.phoneNumberId() != null) {
            existing.setPhoneNumberId(dto.phoneNumberId());
        }
        if (dto.businessAccountId() != null) {
            existing.setBusinessAccountId(dto.businessAccountId());
        }
        if (dto.webhookVerifyToken() != null) {
            existing.setWebhookVerifyToken(dto.webhookVerifyToken());
        }
        if (dto.appId() != null) {
            existing.setAppId(dto.appId());
        }
        if (dto.appSecret() != null) {
            existing.setAppSecretEncrypted(encryptionService.encryptCredential(dto.appSecret()));
        }
        if (dto.displayName() != null) {
            existing.setDisplayName(dto.displayName());
        }
        if (dto.isActive() != null) {
            existing.setIsActive(dto.isActive());
        }

        return repository.save(existing);
    }

    public void deleteConfiguration(Long phoneId) {
        CustomerServicePhone phone = customerServicePhoneService.getById(phoneId);

        if (phone == null) {
            throw new RuntimeException("Customer phone service not found");
        }

        if (phone.getMessagingConfig() != null) {
            Long configId = phone.getMessagingConfig().getId();
            phone.setMessagingConfig(null);
            repository.deleteById(configId);
        }
    }

    public WhatsAppBusinessConfiguration findByPhoneNumberId(String phoneNumberId) {
        return repository.findByPhoneNumberId(phoneNumberId).orElse(null);
    }

    public Optional<WhatsAppBusinessConfiguration> getById(Long configId) {
        return repository.findById(configId);
    }

    public WhatsAppBusinessConfiguration toogleStatus(Long configId) {

        Optional<WhatsAppBusinessConfiguration> configurationOpt = repository
                .findById(configId); // Asumiendo que existe este método

        if (configurationOpt.isEmpty()) {
            throw new RuntimeException("Configuración no encontrada");
        }

        WhatsAppBusinessConfiguration configuration = configurationOpt.get();

        configuration.setIsActive(!configuration.getIsActive());

        return repository
                .save(configuration); // Asumiendo que existe este método
    }

    public WhatsAppBusinessConfiguration getByCustomerServicePhoneId(Long phoneId) {
        CustomerServicePhone phone = customerServicePhoneService.getById(phoneId);

        if (phone == null) {
            throw new RuntimeException("Customer service phone not found");
        }

        WhatsAppBusinessConfiguration configuration = (WhatsAppBusinessConfiguration) phone
                .getMessagingConfig();

        if (configuration == null) {
            throw new RuntimeException("Whatsapp configuration not found");
        }

        return configuration;
    }

    /**
     * Renueva únicamente el access token de una configuración existente
     *
     * @param configId       ID de la configuración
     * @param newAccessToken Nuevo access token
     * @return Configuración actualizada
     */
    public WhatsAppBusinessConfiguration renewAccessToken(Long configId, String newAccessToken) {
        Optional<WhatsAppBusinessConfiguration> configurationOpt = repository.findById(configId);

        if (configurationOpt.isEmpty()) {
            throw new RuntimeException("Configuración de WhatsApp no encontrada");
        }

        WhatsAppBusinessConfiguration configuration = configurationOpt.get();

        // Validar que el nuevo token no esté vacío
        if (newAccessToken == null || newAccessToken.trim().isEmpty()) {
            throw new RuntimeException("El access token no puede estar vacío");
        }

        // Encriptar y actualizar el access token
        configuration.setAccessTokenEncrypted(encryptionService.encryptCredential(newAccessToken));

        return repository.save(configuration);
    }

    /**
     * Renueva el access token usando el phoneId (más común en la práctica)
     *
     * @param phoneId        ID del teléfono de servicio al cliente
     * @param newAccessToken Nuevo access token
     * @return Configuración actualizada
     */
    public WhatsAppBusinessConfiguration renewAccessTokenByPhone(Long phoneId, String newAccessToken) {
        CustomerServicePhone phone = customerServicePhoneService.getById(phoneId);

        if (phone == null) {
            throw new RuntimeException("Teléfono de servicio no encontrado");
        }

        WhatsAppBusinessConfiguration configuration = (WhatsAppBusinessConfiguration) phone.getMessagingConfig();

        if (configuration == null) {
            throw new RuntimeException("No existe configuración de WhatsApp para este teléfono");
        }

        return renewAccessToken(configuration.getId(), newAccessToken);
    }

    /**
     * Renueva los datos críticos que pueden cambiar con frecuencia
     *
     * @param configId              ID de la configuración
     * @param newAccessToken        Nuevo access token (requerido)
     * @param newWebhookVerifyToken Nuevo webhook verify token (opcional)
     * @param newAppSecret          Nuevo app secret (opcional)
     * @return Configuración actualizada
     */
    public WhatsAppBusinessConfiguration renewCriticalCredentials(Long configId,
                                                                  String newAccessToken,
                                                                  String newWebhookVerifyToken,
                                                                  String newAppSecret) {
        // Access token es obligatorio
        if (newAccessToken == null || newAccessToken.trim().isEmpty()) {
            throw new RuntimeException("El access token es requerido");
        }

        Optional<WhatsAppBusinessConfiguration> configurationOpt = repository.findById(configId);

        if (configurationOpt.isEmpty()) {
            throw new RuntimeException("Configuración de WhatsApp no encontrada");
        }

        WhatsAppBusinessConfiguration configuration = configurationOpt.get();

        // Actualizar access token
        configuration.setAccessTokenEncrypted(encryptionService.encryptCredential(newAccessToken));

        // Actualizar webhook verify token si se proporciona
        if (newWebhookVerifyToken != null && !newWebhookVerifyToken.trim().isEmpty()) {
            configuration.setWebhookVerifyToken(newWebhookVerifyToken);
        }

        // Actualizar app secret si se proporciona
        if (newAppSecret != null && !newAppSecret.trim().isEmpty()) {
            configuration.setAppSecretEncrypted(encryptionService.encryptCredential(newAppSecret));
        }

        return repository.save(configuration);
    }

    /**
     * Renueva únicamente el webhook verify token
     *
     * @param configId              ID de la configuración
     * @param newWebhookVerifyToken Nuevo webhook verify token
     * @return Configuración actualizada
     */
    public WhatsAppBusinessConfiguration renewWebhookVerifyToken(Long configId, String newWebhookVerifyToken) {
        Optional<WhatsAppBusinessConfiguration> configurationOpt = repository.findById(configId);

        if (configurationOpt.isEmpty()) {
            throw new RuntimeException("Configuración de WhatsApp no encontrada");
        }

        WhatsAppBusinessConfiguration configuration = configurationOpt.get();

        if (newWebhookVerifyToken == null || newWebhookVerifyToken.trim().isEmpty()) {
            throw new RuntimeException("El webhook verify token no puede estar vacío");
        }

        configuration.setWebhookVerifyToken(newWebhookVerifyToken);

        return repository.save(configuration);
    }

    /**
     * Método para verificar si la configuración necesita renovación
     * (útil para sistemas automatizados de renovación)
     *
     * @param configId ID de la configuración
     * @return true si la configuración es válida y activa
     */
    public boolean isConfigurationHealthy(Long configId) {
        Optional<WhatsAppBusinessConfiguration> configurationOpt = repository.findById(configId);

        if (configurationOpt.isEmpty()) {
            return false;
        }

        WhatsAppBusinessConfiguration configuration = configurationOpt.get();

        return configuration.isConfigurationValid() &&
                configuration.getIsActive() != null &&
                configuration.getIsActive();
    }

    /**
     * Obtiene información básica de la configuración sin datos sensibles
     * (útil para validaciones previas a renovaciones)
     *
     * @param configId ID de la configuración
     * @return Información básica de la configuración
     */
    public ConfigurationSummary getConfigurationSummary(Long configId) {
        Optional<WhatsAppBusinessConfiguration> configurationOpt = repository.findById(configId);

        if (configurationOpt.isEmpty()) {
            throw new RuntimeException("Configuración de WhatsApp no encontrada");
        }

        WhatsAppBusinessConfiguration config = configurationOpt.get();

        return new ConfigurationSummary(
                config.getId(),
                config.getPhoneNumberId(),
                config.getBusinessAccountId(),
                config.getAppId(),
                config.getDisplayName(),
                config.getIsActive(),
                config.isConfigurationValid()
        );
    }

    /**
     * Clase auxiliar para el resumen de configuración
     */
    public record ConfigurationSummary(
            Long id,
            String phoneNumberId,
            String businessAccountId,
            String appId,
            String displayName,
            Boolean isActive,
            boolean isValid
    ) {
    }
}
