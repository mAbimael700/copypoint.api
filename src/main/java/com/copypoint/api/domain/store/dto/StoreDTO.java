package com.copypoint.api.domain.store.dto;

import com.copypoint.api.domain.store.Store;
import com.copypoint.api.domain.user.dto.UserDTO;

import java.time.LocalDateTime;

public record StoreDTO(
        Long id,
        String name,
        UserDTO owner,
        LocalDateTime createdAt
) {
    public StoreDTO(Store savedStore) {
        this(
                savedStore.getId(),
                savedStore.getName(),
                new UserDTO(savedStore.getOwner()),
                savedStore.getCreatedAt()
        );
    }
}
