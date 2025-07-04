package com.copypoint.api.domain.saleprofile.dto;

import com.copypoint.api.domain.saleprofile.SaleProfile;

public record SaleProfileDTO(
        Long saleId,
        Long profileId,
        SaleProfileServiceDTO service,
        String name,
        String description,
        Double unitPrice,
        Integer quantity,
        Double subtotal

) {

    public SaleProfileDTO(SaleProfile saleProfile) {
        this(
                saleProfile.getId().getSaleId(),
                saleProfile.getId().getProfileId(),
                new SaleProfileServiceDTO(saleProfile.getService()),
                saleProfile.getProfile().getName(),
                saleProfile.getProfile().getDescription(),
                saleProfile.getUnitPrice(),
                saleProfile.getQuantity(),
                saleProfile.getSubtotal()
        );
    }
}
