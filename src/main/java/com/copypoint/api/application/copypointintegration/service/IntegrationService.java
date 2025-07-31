package com.copypoint.api.application.copypointintegration.service;

import com.copypoint.api.application.copypointintegration.dto.IntegrationStatsDto;
import com.copypoint.api.application.copypointintegration.dto.MessagingIntegrationDto;
import com.copypoint.api.application.copypointintegration.dto.PaymentIntegrationDto;
import com.copypoint.api.domain.copypoint.Copypoint;
import com.copypoint.api.domain.customerservicephone.CustomerServicePhone;
import com.copypoint.api.domain.customerservicephone.repository.CustomerServicePhoneRepository;
import com.copypoint.api.domain.mercadopagoconfiguration.MercadoPagoConfiguration;
import com.copypoint.api.domain.messagingproviderconfiguration.MessagingProviderConfiguration;
import com.copypoint.api.domain.twilioconfiguration.TwilioConfiguration;
import com.copypoint.api.domain.whatsappbussinessconfiguration.WhatsAppBusinessConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class IntegrationService {

    @Autowired
    private CustomerServicePhoneRepository customerServicePhoneRepository;

    public List<PaymentIntegrationDto> getPaymentIntegrations(Copypoint copypoint) {
        return copypoint.getMercadoPagoConfigurations().stream()
                .map(this::toPaymentIntegrationDto)
                .collect(Collectors.toList());
    }

    public List<MessagingIntegrationDto> getMessagingIntegrations(Copypoint copypoint) {
        List<CustomerServicePhone> phones =
                customerServicePhoneRepository.findByCopypointIdWithMessagingConfig(copypoint.getId());

        return phones.stream()
                .filter(phone -> phone.getMessagingConfig() != null)
                .map(this::toMessagingIntegrationDto)
                .collect(Collectors.toList());
    }

    public IntegrationStatsDto calculateStats(List<PaymentIntegrationDto> payment,
                                              List<MessagingIntegrationDto> messaging) {
        int totalIntegrations = payment.size() + messaging.size();
        long activeIntegrations = payment.stream().filter(PaymentIntegrationDto::isActive).count() +
                messaging.stream().filter(MessagingIntegrationDto::isActive).count();

        return new IntegrationStatsDto(
                totalIntegrations,
                (int) activeIntegrations,
                payment.size(),
                messaging.size()
        );
    }

    private PaymentIntegrationDto toPaymentIntegrationDto(MercadoPagoConfiguration config) {
        return PaymentIntegrationDto.fromMercadoPagoConfiguration(
                config.getId(),
                "Mercado Pago",
                "Mercado Pago Checkout",
                config.getIsActive() != null ? config.getIsActive() : false,
                isConfigurationValid(config),
                config.getCreatedAt(),
                config.getUpdatedAt(),
                config.getClientId(),
                config.getVendorEmail(),
                config.getIsSandbox() != null ? config.getIsSandbox() : false
        );
    }

    private MessagingIntegrationDto toMessagingIntegrationDto(CustomerServicePhone phone) {
        MessagingProviderConfiguration config = phone.getMessagingConfig();

        return switch (config.getProviderType()) {
            case WHATSAPP_BUSINESS_API -> createWhatsAppDto(phone, (WhatsAppBusinessConfiguration) config);
            case TWILIO -> createTwilioDto(phone, (TwilioConfiguration) config);
            default -> throw new IllegalArgumentException("Unsupported provider type: " + config.getProviderType());
        };
    }

    private MessagingIntegrationDto createWhatsAppDto(CustomerServicePhone phone,
                                                      WhatsAppBusinessConfiguration config) {
        return MessagingIntegrationDto.fromWhatsAppConfiguration(
                config.getId(),
                config.getIsActive() != null ? config.getIsActive() : false,
                config.isConfigurationValid(),
                phone.getPhoneNumber(),
                config.getBusinessAccountId(),
                config.getPhoneNumberId()
        );
    }

    private MessagingIntegrationDto createTwilioDto(CustomerServicePhone phone,
                                                    TwilioConfiguration config) {
        return MessagingIntegrationDto.fromTwilioConfiguration(
                config.getId(),
                config.getIsActive() != null ? config.getIsActive() : false,
                config.isConfigurationValid(),
                phone.getPhoneNumber()
        );
    }

    private boolean isConfigurationValid(MercadoPagoConfiguration config) {
        return config.getAccessTokenEncrypted() != null && !config.getAccessTokenEncrypted().trim().isEmpty() &&
                config.getPublicKeyEncrypted() != null && !config.getPublicKeyEncrypted().trim().isEmpty() &&
                config.getClientSecretEncrypted() != null && !config.getClientSecretEncrypted().trim().isEmpty() &&
                config.getClientId() != null && !config.getClientId().trim().isEmpty();
    }

}
