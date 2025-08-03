package com.copypoint.api.domain.mercadopagoconfiguration.controller;

import com.copypoint.api.domain.mercadopagoconfiguration.MercadoPagoConfiguration;
import com.copypoint.api.domain.mercadopagoconfiguration.dto.MercadoPagoConfigurationCreationDTO;
import com.copypoint.api.domain.mercadopagoconfiguration.dto.MercadoPagoConfigurationDTO;
import com.copypoint.api.domain.mercadopagoconfiguration.service.MercadoPagoConfigurationService;
import com.copypoint.api.infra.security.utils.SecureTokenGenerator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MercadoPagoConfigurationController {

    @Autowired
    private MercadoPagoConfigurationService mercadoPagoConfigurationService;

    @Autowired
    private SecureTokenGenerator secureTokenGenerator;

    @PostMapping("/copypoints/{copypointId}/mercadopago-config")
    public ResponseEntity<String> createMercadoPagoConfiguration(
            @PathVariable Long copypointId,
            @Valid @RequestBody MercadoPagoConfigurationCreationDTO creationDTO
    ) {
        mercadoPagoConfigurationService.saveConfiguration(copypointId, creationDTO);
        return ResponseEntity.ok("Configuración de mercado pago configurada");

    }

    @GetMapping("/copypoints/{copypointId}/mercadopago-config")
    public ResponseEntity<List<MercadoPagoConfigurationDTO>> getByCopypoint(
            @PathVariable Long copypointId
    ) {
        List<MercadoPagoConfiguration> mercadoPagoConfigurations =
                mercadoPagoConfigurationService.getByCopypoint(copypointId);

        return ResponseEntity.ok(
                mercadoPagoConfigurations.stream()
                        .map(MercadoPagoConfigurationDTO::new)
                        .toList());
    }

    @GetMapping("/mercadopago-config/generate-token")
    public ResponseEntity<String> generateMercadoPagoWebhookToken() {
        return ResponseEntity.ok(secureTokenGenerator.generateMercadoPagoToken());
    }
}
