package com.copypoint.api.infra.http.controller;

import com.copypoint.api.domain.service.ServiceService;
import com.copypoint.api.domain.service.dto.ServiceCreationDTO;
import com.copypoint.api.domain.service.dto.ServiceDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/stores/{storeId}/services")
public class ServiceController {

    @Autowired
    private ServiceService serviceService;

    @PreAuthorize("hasAuthority('MODULE_STORE_MANAGEMENT')")
    @GetMapping
    public ResponseEntity<Page<ServiceDTO>> getServices(
            Pageable pageable,
            @PathVariable Long storeId) {
        var services = serviceService.getAllByStoreId(pageable, storeId);
        return ResponseEntity.ok(services);
    }


    @PreAuthorize("hasAuthority('MODULE_STORE_MANAGEMENT')")
    @PostMapping
    public ResponseEntity<ServiceDTO> createService(
            @PathVariable Long storeId,
            @Valid @RequestBody ServiceCreationDTO creationDTO
    ) {
        var savedService = serviceService.create(creationDTO, storeId);
        URI location = URI.create("/api/stores/" + storeId + "/services/" + savedService.id());
        return ResponseEntity.created(location).body(savedService);
    }
}
