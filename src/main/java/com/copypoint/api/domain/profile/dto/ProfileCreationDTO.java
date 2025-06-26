package com.copypoint.api.domain.profile.dto;

import java.util.List;

public record ProfileCreationDTO(
        List<Long> services,
        String name,
        String description,
        Double unitPrice
) {

}
