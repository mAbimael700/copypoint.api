package com.copypoint.api.domain.service.dto;

import com.copypoint.api.domain.service.Service;

public record ServiceDTO(
        Long id,
        String name,
        Boolean status

) {
    public ServiceDTO(Service service) {
        this(
                service.getId(),
                service.getName(),
                service.getActive()
        );
    }
}
