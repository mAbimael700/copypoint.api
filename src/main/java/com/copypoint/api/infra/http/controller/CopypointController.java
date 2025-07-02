package com.copypoint.api.infra.http.controller;

import com.copypoint.api.domain.copypoint.service.CopypointService;
import com.copypoint.api.domain.copypoint.dto.CopypointCreationDTO;
import com.copypoint.api.domain.copypoint.dto.CopypointDTO;
import com.copypoint.api.infra.http.userprincipal.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/stores/{storeId}/copypoints")
public class CopypointController {
    @Autowired
    private CopypointService copypointService;

    @PreAuthorize("hasAuthority('MODULE_COPYPOINT_MANAGEMENT')")
    @PostMapping
    public ResponseEntity<CopypointDTO> createCopypoint(
            @RequestBody @Valid CopypointCreationDTO creationDTO,
            @PathVariable Long storeId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal.getUser().getId();
        var createdCopypoint = copypointService.create(creationDTO, userId, storeId);
        URI location = URI.create("/api/stores/" + storeId + "/copypoints/" + createdCopypoint.id());
        return ResponseEntity.created(location).body(createdCopypoint);
    }

    @GetMapping
    public ResponseEntity<Page<CopypointDTO>> getAll(
            Pageable pageable,
            @PathVariable Long storeId
    ) {
        return ResponseEntity.ok(copypointService.getlAllByStoreId(pageable, storeId));
    }
}
