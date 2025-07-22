package com.copypoint.api.domain.whatsappbussinessconfiguration.controller;

import com.copypoint.api.domain.whatsappbussinessconfiguration.WhatsAppBusinessConfiguration;
import com.copypoint.api.domain.whatsappbussinessconfiguration.dto.WhatsAppConfigurationDTO;
import com.copypoint.api.domain.whatsappbussinessconfiguration.dto.WhatsAppConfigurationResponseDTO;
import com.copypoint.api.domain.whatsappbussinessconfiguration.service.WhatsAppBusinessConfigurationService;
import com.copypoint.api.infra.whatsappbusiness.util.WhatsAppWebhookTokenGenerator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/whatsapp-config")
public class WhatsAppBusinessConfigurationController {

    @Autowired
    private WhatsAppBusinessConfigurationService configurationService;

    @Autowired
    private WhatsAppWebhookTokenGenerator tokenGenerator;

    @PostMapping(value = "/phone/{phoneId}")
    public ResponseEntity<?> createConfiguration(
            @PathVariable Long phoneId,
            @Valid @RequestBody WhatsAppConfigurationDTO configurationDTO) {

        try {
            WhatsAppBusinessConfiguration configuration = configurationService
                    .createConfiguration(phoneId, configurationDTO);

            return ResponseEntity.status(HttpStatus.CREATED).body(configuration);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al crear la configuración" + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al crear la configuración: " + e.getMessage());
        }
    }

    @PutMapping("/{configId}")
    public ResponseEntity<?> updateConfiguration(
            @PathVariable Long configId,
            @Valid @RequestBody WhatsAppConfigurationDTO configurationDTO) {

        try {

            WhatsAppBusinessConfiguration updated = configurationService
                    .updateConfiguration(configId, configurationDTO);

            return ResponseEntity.ok(new WhatsAppConfigurationResponseDTO(updated));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar la configuración: " + e.getMessage());
        }
    }

    @GetMapping("/{configId}")
    public ResponseEntity<?> getConfiguration(
            @PathVariable Long configId) {

        Optional<WhatsAppBusinessConfiguration> configuration = configurationService
                .getById(configId); // Asumiendo que existe este método

        if (configuration.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(new WhatsAppConfigurationResponseDTO(configuration.get()));
    }

    @GetMapping("/phone-number/{phoneNumberId}")
    public ResponseEntity<WhatsAppBusinessConfiguration> getConfigurationByPhoneNumberId(
            @PathVariable String phoneNumberId) {

        WhatsAppBusinessConfiguration configuration = configurationService
                .findByPhoneNumberId(phoneNumberId);

        if (configuration == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(configuration);
    }

    @GetMapping("/phone/{phoneId}")
    public ResponseEntity<WhatsAppConfigurationResponseDTO> getConfigurationByPhone(
            @PathVariable Long phoneId) {

        WhatsAppBusinessConfiguration configuration = configurationService
                .getByCustomerServicePhoneId(phoneId);

        return ResponseEntity.ok(new WhatsAppConfigurationResponseDTO(configuration));
    }

    @DeleteMapping("/phone/{phoneId}")
    public ResponseEntity<?> deleteConfiguration(
            @PathVariable Long phoneId) {

        try {

            configurationService.deleteConfiguration(phoneId);
            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar la configuración: " + e.getMessage());
        }
    }

    @PatchMapping("/{configId}/toggle-status")
    public ResponseEntity<?> toggleConfigurationStatus(
            @PathVariable Long configId) {

        try {
            WhatsAppBusinessConfiguration updated = configurationService.toogleStatus(configId);

            return ResponseEntity.ok(updated);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al cambiar el estado: " + e.getMessage());
        }
    }

    @GetMapping("/generate-token")
    public ResponseEntity<String> getWebhookToken() {
        return ResponseEntity.ok(tokenGenerator.generateVerifyToken());
    }
}
