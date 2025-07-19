package com.copypoint.api.domain.contact.service;

import com.copypoint.api.domain.contact.Contact;
import com.copypoint.api.domain.contact.repository.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ContactService implements IContactService {
    @Autowired
    private ContactRepository contactRepository;

    @Override
    public Contact findByPhoneNumber(String phoneNumber) {
        return contactRepository.findByPhoneNumber(phoneNumber).orElse(null);
    }

    @Override
    public Contact save(Contact contact) {
        return contactRepository.save(contact);
    }
}
