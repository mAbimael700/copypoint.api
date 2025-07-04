package com.copypoint.api.domain.copypoint.dto;

import com.copypoint.api.domain.copypoint.Copypoint;
import com.copypoint.api.domain.store.Store;
import com.copypoint.api.domain.user.dto.UserDTO;

import java.time.LocalDateTime;

public record CopypointDTO(
        Long id,
        String name,
        UserDTO responsible,
        UserDTO createdBy,
        String status,
        LocalDateTime creationDate,
        LocalDateTime lastModified
) {
    public CopypointDTO(Copypoint copypoint) {
        this(
                copypoint.getId(),
                copypoint.getName(),
                new UserDTO(copypoint.getResponsible()),
                new UserDTO(copypoint.getCreatedBy()),
                copypoint.getStatus().toString(),
                copypoint.getCreatedAt(),
                copypoint.getLastModifiedAt()
        );
    }

}
