package com.copypoint.api.infra.http.controller;

import com.copypoint.api.domain.profile.service.ProfileService;
import com.copypoint.api.domain.profile.dto.ProfileCreationDTO;
import com.copypoint.api.domain.profile.dto.ProfileDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api")
public class ProfileController {
    @Autowired
    private ProfileService profileService;


    @GetMapping("/stores/{storeId}/profiles")
    public ResponseEntity<Page<ProfileDTO>> getByServiceId(
            @RequestParam Long serviceId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(profileService.getProfilesByServiceId(serviceId, pageable));
    }

    @PostMapping("/stores/{storeId}/profiles")
    public ResponseEntity<ProfileDTO> createProfile(
            @PathVariable Long storeId,
            @RequestBody ProfileCreationDTO creationDTO
    ) {
        ProfileDTO savedProfile = profileService.createProfile(creationDTO);
        URI location = URI.create("/api/stores/" + storeId + "/profiles/" + savedProfile.id());
        return ResponseEntity.created(location).body(savedProfile);
    }

    @GetMapping("/copypoints/{copypointId}/services/{serviceId}/profiles")
    public ResponseEntity<Page<ProfileDTO>> getByCopypointAndService(
            @PathVariable Long copypointId,
            @PathVariable Long serviceId,
            Pageable pageable
    ) {
        Page<ProfileDTO> profiles = profileService.getProfilesByServiceId(serviceId, pageable);
        return ResponseEntity.ok(profiles);
    }
}
