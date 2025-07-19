package com.copypoint.api.domain.customerservicephone.service;

import com.copypoint.api.domain.customerservicephone.CustomerServicePhone;
import com.copypoint.api.domain.customerservicephone.repository.CustomerServicePhoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomerServicePhoneService implements ICustomerServicePhoneService {

    @Autowired
    private CustomerServicePhoneRepository customerServicePhoneRepository;

    @Override
    public CustomerServicePhone findById(Long id) {
        return customerServicePhoneRepository.findById(id).orElse(null);
    }
}
