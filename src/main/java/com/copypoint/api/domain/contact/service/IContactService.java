package com.copypoint.api.domain.contact.service;

import com.copypoint.api.domain.contact.Contact;

public interface IContactService {
    Contact findByPhoneNumber(String phoneNumber);
    Contact save(Contact contact);
}
