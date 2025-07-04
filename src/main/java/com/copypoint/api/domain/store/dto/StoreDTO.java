package com.copypoint.api.domain.store.dto;

import com.copypoint.api.domain.store.Store;
import com.copypoint.api.domain.user.dto.UserDTO;

import java.time.LocalDateTime;

public record StoreDTO(
        Long id,
        String name,
        String currency,
        UserDTO owner,
        LocalDateTime createdAt
) {
    public StoreDTO(Store savedStore) {
        this(
                savedStore.getId(),
                savedStore.getName(),
                savedStore.getCurrency(),
                new UserDTO(savedStore.getOwner()),
                savedStore.getCreatedAt()
        );
    }
}
