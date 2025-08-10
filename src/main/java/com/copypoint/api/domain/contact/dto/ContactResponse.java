package com.copypoint.api.domain.contact.dto;

import com.copypoint.api.domain.contact.Contact;

public record ContactResponse(
        Long id,
        String phoneNumber,
        String displayName
) {

    public ContactResponse(Contact customerContact) {
        this(
                customerContact.getId(),
                customerContact.getPhoneNumber(),
                customerContact.getDisplayName()
        );
    }
}
