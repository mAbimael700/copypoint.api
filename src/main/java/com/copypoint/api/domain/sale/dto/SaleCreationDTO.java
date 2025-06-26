package com.copypoint.api.domain.sale.dto;

import com.copypoint.api.domain.saleprofile.dto.SaleProfileCreationDTO;

import java.util.List;

public record SaleCreationDTO(
        String currency,
        Long paymentMethodId,
        List<SaleProfileCreationDTO> profiles
) {
}
