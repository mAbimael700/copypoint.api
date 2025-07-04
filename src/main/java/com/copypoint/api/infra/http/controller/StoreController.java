package com.copypoint.api.infra.http.controller;

import com.copypoint.api.domain.copypoint.service.CopypointService;
import com.copypoint.api.domain.store.dto.StoreCreationDTO;
import com.copypoint.api.domain.store.dto.StoreDTO;
import com.copypoint.api.domain.store.service.StoreService;
import com.copypoint.api.infra.http.userprincipal.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/stores")
public class StoreController {

    @Autowired
    private StoreService storeService;

    @Autowired
    private CopypointService copypointService;

    @GetMapping
    public ResponseEntity<Page<StoreDTO>> getAll(Pageable pageable) {
         Page<StoreDTO> stores = storeService.getAll(pageable);
         return ResponseEntity.ok(stores);
    }

    @PostMapping
    public ResponseEntity<StoreDTO> createStore(
            @RequestBody @Valid StoreCreationDTO creationDTO,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        // Más limpio con método de conveniencia
        Long userId = userPrincipal.getUser().getId();

        var createdStore = storeService.createStore(creationDTO, userId);
        URI location = URI.create("/api/stores/" + createdStore.id());
        return ResponseEntity.created(location).body(createdStore);
    }

}
