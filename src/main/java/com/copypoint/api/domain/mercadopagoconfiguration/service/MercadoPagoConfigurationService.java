package com.copypoint.api.domain.mercadopagoconfiguration.service;

import com.copypoint.api.domain.mercadopagoconfiguration.MercadoPagoConfiguration;
import com.copypoint.api.domain.mercadopagoconfiguration.repository.MercadoPagoConfigurationRepository;
import com.copypoint.api.domain.sale.Sale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MercadoPagoConfigurationService {

    @Autowired
    private MercadoPagoConfigurationRepository mercadoPagoConfigRepository;

    /**
     * Obtiene la configuración de MercadoPago para una sale específica
     * Prioridad: Copypoint > Store
     */
    public Optional<MercadoPagoConfiguration> getConfigForSale(Sale sale) {
        Long copypointId = sale.getCopypoint().getId();
        return mercadoPagoConfigRepository.findActiveByCopypointId(copypointId);
    }

    /**
     * Configura MercadoPago SDK con el token correcto para la sale
     */
    public boolean configureForSale(Sale sale) {
        Optional<MercadoPagoConfiguration> configOpt = getConfigForSale(sale);

        if (configOpt.isPresent()) {
            MercadoPagoConfiguration config = configOpt.get();
            com.mercadopago.MercadoPagoConfig.setAccessToken(config.getAccessToken());

            // Configurar sandbox si es necesario
            if (config.getIsSandbox() != null && config.getIsSandbox()) {
                System.setProperty("mercadopago.sandbox", "true");
            }

            return true;
        }

        return false;
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
            return configOpt.get().getWebhookSecret();
        }

        return null;
    }
}
