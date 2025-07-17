package com.copypoint.api.infra.http.controller;

import com.copypoint.api.domain.mercadopagoconfiguration.dto.MercadoPagoConfigurationCreationDTO;
import com.copypoint.api.domain.mercadopagoconfiguration.service.MercadoPagoConfigurationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class MercadoPagoConfigurationController {

    @Autowired
    private MercadoPagoConfigurationService mercadoPagoConfigurationService;

    @PostMapping("/copypoints/{copypointId}/mercadopago-config")
    public ResponseEntity<String> createMercadoPagoConfiguration(
            @PathVariable Long copypointId,
            @Valid @RequestBody MercadoPagoConfigurationCreationDTO creationDTO
    ) {
        mercadoPagoConfigurationService.saveConfiguration(copypointId, creationDTO);
        return ResponseEntity.ok("Configuración de mercado pago configurada");

    }
}
