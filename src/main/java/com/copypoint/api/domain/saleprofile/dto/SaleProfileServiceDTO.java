package com.copypoint.api.domain.saleprofile.dto;

import com.copypoint.api.domain.service.Service;

public record SaleProfileServiceDTO(
        Long id,
        String name
) {
    public SaleProfileServiceDTO(Service service){
        this(service.getId(), service.getName());
    }
}
