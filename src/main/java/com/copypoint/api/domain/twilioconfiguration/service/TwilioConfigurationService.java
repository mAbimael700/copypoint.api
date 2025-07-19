package com.copypoint.api.domain.twilioconfiguration.service;

import com.copypoint.api.domain.twilioconfiguration.TwilioConfiguration;
import com.copypoint.api.domain.twilioconfiguration.dto.TwilioConfigurationCreationRequest;
import com.copypoint.api.domain.twilioconfiguration.dto.TwilioConfigurationUpdateRequest;
import com.copypoint.api.domain.twilioconfiguration.repository.TwilioConfigurationRepository;
import com.copypoint.api.infra.security.service.CredentialEncryptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TwilioConfigurationService {

    private static final Logger logger = LoggerFactory.getLogger(TwilioConfigurationService.class);

    @Autowired
    private TwilioConfigurationRepository twilioConfigurationRepository;

    @Autowired
    private CredentialEncryptionService credentialEncryptionService;

    /**
     * Crea una nueva configuración de Twilio
     *
     * @param request DTO con los datos de configuración
     * @return Configuración creada
     */
    public TwilioConfiguration createTwilioConfiguration(TwilioConfigurationCreationRequest request) {
        logger.info("Creando nueva configuración de Twilio para: {}", request.displayName());

        try {
            // Encriptar credenciales sensibles
            String encryptedAccountSid = credentialEncryptionService.encryptCredential(request.accountSid());
            String encryptedAuthToken = credentialEncryptionService.encryptCredential(request.authToken());

            // Crear entidad
            TwilioConfiguration configuration = TwilioConfiguration.builder()
                    .encryptedAccountSid(encryptedAccountSid)
                    .encryptedAuthToken(encryptedAuthToken)
                    .webhookUrl(request.webhookUrl())
                    .statusCallbackUrl(request.statusCallbackUrl())
                    .displayName(request.displayName())
                    .isActive(request.isActive() != null ? request.isActive() : true)
                    .build();

            // Guardar en base de datos
            TwilioConfiguration saved = twilioConfigurationRepository.save(configuration);

            logger.info("Configuración de Twilio creada exitosamente con ID: {}", saved.getId());
            return saved;

        } catch (Exception e) {
            logger.error("Error al crear configuración de Twilio: {}", e.getMessage(), e);
            throw new RuntimeException("Error al crear configuración de Twilio", e);
        }
    }


    /**
     * Actualiza una configuración existente de Twilio
     *
     * @param id      ID de la configuración
     * @param request DTO con los datos actualizados
     * @return Configuración actualizada
     */
    public TwilioConfiguration updateTwilioConfiguration(Long id, TwilioConfigurationUpdateRequest request) {
        logger.info("Actualizando configuración de Twilio ID: {}", id);

        TwilioConfiguration existing = findById(id);

        try {
            // Actualizar campos si se proporcionan nuevos valores
            if (request.accountSid() != null
                    && !request.accountSid().trim().isEmpty()) {

                String encryptedAccountSid = credentialEncryptionService
                        .encryptCredential(request.accountSid());

                existing.setEncryptedAccountSid(encryptedAccountSid);
            }

            if (request.authToken() != null && !request.authToken().trim().isEmpty()) {
                String encryptedAuthToken = credentialEncryptionService
                        .encryptCredential(request.authToken());

                existing.setEncryptedAuthToken(encryptedAuthToken);
            }

            if (request.webhookUrl() != null) {
                existing.setWebhookUrl(request.webhookUrl());
            }

            if (request.statusCallbackUrl() != null) {
                existing.setStatusCallbackUrl(request.statusCallbackUrl());
            }

            if (request.displayName() != null) {
                existing.setDisplayName(request.displayName());
            }

            if (request.isActive() != null) {
                existing.setIsActive(request.isActive());
            }

            TwilioConfiguration updated = twilioConfigurationRepository.save(existing);
            logger.info("Configuración de Twilio actualizada exitosamente");
            return updated;

        } catch (Exception e) {
            logger.error("Error al actualizar configuración de Twilio: {}", e.getMessage(), e);
            throw new RuntimeException("Error al actualizar configuración de Twilio", e);
        }
    }


    /**
     * Obtiene una configuración por ID
     *
     * @param id ID de la configuración
     * @return Configuración encontrada
     */
    @Transactional(readOnly = true)
    public TwilioConfiguration findById(Long id) {
        return twilioConfigurationRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Configuración de Twilio no encontrada con ID: " + id));
    }

    /**
     * Obtiene todas las configuraciones activas
     * @return Lista de configuraciones activas
     */
    @Transactional(readOnly = true)
    public List<TwilioConfiguration> findActiveConfigurations() {
        return twilioConfigurationRepository.findByIsActiveTrue();
    }

    /**
     * Obtiene todas las configuraciones
     * @return Lista de todas las configuraciones
     */
    @Transactional(readOnly = true)
    public List<TwilioConfiguration> findAllConfigurations() {
        return twilioConfigurationRepository.findAll();
    }


    /**
     * Desactiva una configuración (soft delete)
     * @param id ID de la configuración
     */
    public void deactivateConfiguration(Long id) {
        logger.info("Desactivando configuración de Twilio ID: {}", id);

        TwilioConfiguration configuration = findById(id);
        configuration.setIsActive(false);
        twilioConfigurationRepository.save(configuration);

        logger.info("Configuración de Twilio desactivada exitosamente");
    }

    /**
     * Activa una configuración
     * @param id ID de la configuración
     */
    public void activateConfiguration(Long id) {
        logger.info("Activando configuración de Twilio ID: {}", id);

        TwilioConfiguration configuration = findById(id);
        configuration.setIsActive(true);
        twilioConfigurationRepository.save(configuration);

        logger.info("Configuración de Twilio activada exitosamente");
    }

    /**
     * Elimina permanentemente una configuración
     * @param id ID de la configuración
     */
    public void deleteConfiguration(Long id) {
        logger.info("Eliminando configuración de Twilio ID: {}", id);

        if (!twilioConfigurationRepository.existsById(id)) {
            throw new RuntimeException("Configuración de Twilio no encontrada con ID: " + id);
        }

        twilioConfigurationRepository.deleteById(id);
        logger.info("Configuración de Twilio eliminada exitosamente");
    }


}
