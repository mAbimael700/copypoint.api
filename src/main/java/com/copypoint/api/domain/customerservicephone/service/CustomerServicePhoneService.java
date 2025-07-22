package com.copypoint.api.domain.customerservicephone.service;

import com.copypoint.api.domain.copypoint.Copypoint;
import com.copypoint.api.domain.copypoint.repository.CopypointRepository;
import com.copypoint.api.domain.customerservicephone.CustomerServicePhone;
import com.copypoint.api.domain.customerservicephone.dto.CustomerServicePhoneCreationDTO;
import com.copypoint.api.domain.customerservicephone.dto.CustomerServicePhoneUpdateDTO;
import com.copypoint.api.domain.customerservicephone.repository.CustomerServicePhoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomerServicePhoneService implements ICustomerServicePhoneService {

    @Autowired
    private CustomerServicePhoneRepository customerServicePhoneRepository;

    @Autowired
    private CopypointRepository copypointRepository;

    @Override
    public CustomerServicePhone getById(Long id) {
        return customerServicePhoneRepository.findById(id).orElse(null);
    }

    public CustomerServicePhone save(CustomerServicePhoneCreationDTO creationDTO,
                                     Long copypointId) {

        Optional<CustomerServicePhone> customerServicePhoneOpt = customerServicePhoneRepository
                .findByPhoneNumber(creationDTO.phoneNumber());

        if (customerServicePhoneOpt.isPresent()) {
            throw new RuntimeException("The phone number is already taken");
        }

        Optional<Copypoint> copypoint = copypointRepository.findById(copypointId);

        if (copypoint.isEmpty()) {
            throw new RuntimeException("Copypoint with Id not found");
        }

        CustomerServicePhone newPhone = CustomerServicePhone.builder()
                .phoneNumber(creationDTO.phoneNumber())
                .copypoint(copypoint.get())
                .build();

        return customerServicePhoneRepository.save(newPhone);
    }

    public Page<CustomerServicePhone> getByCopypointId(Long copypointId, Pageable pageable) {
        return customerServicePhoneRepository.findByCopypointId(copypointId, pageable);
    }

    public CustomerServicePhone update(Long customerServicePhoneId, CustomerServicePhoneUpdateDTO updateDTO) {

        Optional<CustomerServicePhone> customerServicePhoneOptional = customerServicePhoneRepository
                .findById(customerServicePhoneId);

        if (customerServicePhoneOptional.isEmpty()) {
            throw new RuntimeException("The phone number does not exists");
        }

        Optional<CustomerServicePhone> customerServicePhoneOpt = customerServicePhoneRepository
                .findByPhoneNumber(updateDTO.phoneNumber());

        if (customerServicePhoneOpt.isPresent() &&
                !customerServicePhoneOpt.get()
                        .getId().equals(customerServicePhoneId)
        ) {
            throw new RuntimeException("The phone number is already taken");
        }

        customerServicePhoneOptional.get()
                .setPhoneNumber(updateDTO.phoneNumber());

        return customerServicePhoneRepository.save(customerServicePhoneOptional.get());
    }
}
