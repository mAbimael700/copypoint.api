package com.copypoint.api.infra.mercadopago.service;

import com.copypoint.api.domain.mercadopagoconfiguration.MercadoPagoConfiguration;
import com.copypoint.api.domain.mercadopagoconfiguration.service.MercadoPagoConfigurationService;
import com.copypoint.api.domain.sale.Sale;
import com.copypoint.api.infra.security.service.CredentialEncryptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MercadoPagoGatewayConfigurationService {

    private static final Logger logger = LoggerFactory.getLogger(MercadoPagoGatewayConfigurationService.class);

    @Autowired
    private CredentialEncryptionService encryptionService;

    @Autowired
    private MercadoPagoConfigurationService mercadoPagoConfigurationService;

    /**
     * Configura MercadoPago SDK con el token correcto para la sale
     */
    public boolean configureForSale(Sale sale) {
        try {
            Optional<MercadoPagoConfiguration> configOpt = mercadoPagoConfigurationService.getConfigForSale(sale);

            if (configOpt.isPresent()) {
                MercadoPagoConfiguration config = configOpt.get();

                String accessToken = getDecryptedAccessToken(config);

                if (accessToken != null) {
                    com.mercadopago.MercadoPagoConfig.setAccessToken(accessToken);

                    if (config.getIsSandbox() != null && config.getIsSandbox()) {
                        System.setProperty("mercadopago.sandbox", "true");
                    } else {
                        System.setProperty("mercadopago.sandbox", "false");
                    }

                    logger.info("MercadoPago SDK configurado exitosamente para copypoint: {}",
                            sale.getCopypoint().getId());
                    return true;
                } else {
                    logger.error("No se pudo obtener el access token para copypoint: {}",
                            sale.getCopypoint().getId());
                }
            } else {
                logger.warn("No se encontró configuración de MercadoPago para copypoint: {}",
                        sale.getCopypoint().getId());
            }
            return false;
        } catch (Exception e) {
            logger.error("Error al configurar MercadoPago SDK: {}", e.getMessage());
            return false;
        }
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
}
