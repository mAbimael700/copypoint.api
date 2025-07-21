package com.copypoint.api.domain.whatsappbussinessconfiguration.service;

import com.copypoint.api.domain.customerservicephone.CustomerServicePhone;
import com.copypoint.api.domain.customerservicephone.service.CustomerServicePhoneService;
import com.copypoint.api.domain.whatsappbussinessconfiguration.WhatsAppBusinessConfiguration;
import com.copypoint.api.domain.whatsappbussinessconfiguration.repository.WhatsAppBusinessConfigurationRepository;
import com.copypoint.api.infra.security.service.CredentialEncryptionService;
import com.copypoint.api.domain.whatsappbussinessconfiguration.dto.WhatsAppConfigurationDTO;
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

        CustomerServicePhone phone = customerServicePhoneService.findById(phoneId);

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
        CustomerServicePhone phone = customerServicePhoneService.findById(phoneId);

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
        CustomerServicePhone phone = customerServicePhoneService.findById(phoneId);

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
}
