package com.copypoint.api.infra.mercadopagocheckout.service;

import com.copypoint.api.domain.mercadopagoconfiguration.MercadoPagoConfiguration;
import com.copypoint.api.domain.mercadopagoconfiguration.service.MercadoPagoConfigurationService;
import com.copypoint.api.domain.sale.Sale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MercadoPagoGatewayConfigurationService {

    @Autowired
    private MercadoPagoConfigurationService mercadoPagoConfigurationService;

    /**
     * Configura MercadoPago SDK con el token correcto para la sale
     */
    public boolean configureForSale(Sale sale) {
        Optional<MercadoPagoConfiguration> configOpt = mercadoPagoConfigurationService.getConfigForSale(sale);

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
}
