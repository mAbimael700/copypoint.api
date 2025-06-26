package com.copypoint.api.domain.profile.dto;

import com.copypoint.api.domain.profile.Profile;
import com.copypoint.api.domain.service.dto.ServiceDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public record ProfileDTO(
        Long id,
        String name,
        String description,
        Double unitPrice,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        Boolean status,
        List<ServiceDTO> services
) {
    public ProfileDTO(Profile profile) {
        this(
                profile.getId(),
                profile.getName(),
                profile.getDescription(),
                profile.getUnitPrice(),
                profile.getCreatedAt(),
                profile.getLastModifiedAt(),
                profile.getActive(),
                profile.getServices().stream().map(ServiceDTO::new).toList()
        );
    }
}
