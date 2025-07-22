package com.copypoint.api.domain.customerservicephone.dto;

import com.copypoint.api.domain.customerservicephone.CustomerServicePhone;

public record CustomerServicePhoneDTO(
        Long id,
        String phoneNumber
) {
    public CustomerServicePhoneDTO(CustomerServicePhone phone) {
        this(
                phone.getId(),
                phone.getPhoneNumber()
        );
    }
}
