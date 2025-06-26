package com.copypoint.api.domain.sale.dto;

import com.copypoint.api.domain.paymentmethod.PaymentMethod;
import com.copypoint.api.domain.paymentmethod.dto.PaymentMethodDTO;
import com.copypoint.api.domain.sale.Sale;
import com.copypoint.api.domain.sale.SaleStatus;
import com.copypoint.api.domain.service.dto.ServiceDTO;
import com.copypoint.api.domain.user.dto.UserDTO;

import java.time.LocalDateTime;

public record SaleDTO(
        Long id,
        UserDTO userVendor,
        //ServiceDTO saleInformation,
        PaymentMethodDTO paymentMethod,
        Double total,
        String currency,
        SaleStatus status,
        Double discount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Integer totalProfiles
) {
    public SaleDTO(Sale sale) {
        this(
                sale.getId(),
                new UserDTO(sale.getUserVendor()),
                new PaymentMethodDTO(sale.getPaymentMethod()),
                sale.getTotal(),
                sale.getCurrency(),
                sale.getStatus(),
                sale.getDiscount(),
                sale.getCreatedAt(),
                sale.getUpdatedAt(),
                sale.getSaleProfiles().size()
        );
    }
}
