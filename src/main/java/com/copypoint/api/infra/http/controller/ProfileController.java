package com.copypoint.api.infra.http.controller;

import com.copypoint.api.domain.profile.ProfileService;
import com.copypoint.api.domain.profile.dto.ProfileCreationDTO;
import com.copypoint.api.domain.profile.dto.ProfileDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/stores/{storeId}/profiles")
public class ProfileController {
    @Autowired
    private ProfileService profileService;


    @GetMapping
    public ResponseEntity<Page<ProfileDTO>> getByServiceId(
            @RequestParam Long serviceId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(profileService.getProfilesByServiceId(serviceId, pageable));
    }

    @PostMapping
    public ResponseEntity<ProfileDTO> createProfile(
            @PathVariable Long storeId,
            @RequestBody ProfileCreationDTO creationDTO
    ) {
        ProfileDTO savedProfile = profileService.createProfile(creationDTO);
        URI location = URI.create("/api/stores/" + storeId + "/profiles/" + savedProfile.id());
        return ResponseEntity.created(location).body(savedProfile);
    }

}
