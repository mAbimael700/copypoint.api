package com.copypoint.api.domain.paymentmethod.service;

import com.copypoint.api.domain.paymentmethod.dto.PaymentMethodDTO;
import com.copypoint.api.domain.paymentmethod.repository.PaymentMethodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class PaymentMethodService {
    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    public Page<PaymentMethodDTO> getAll(Pageable pageable) {
        return paymentMethodRepository.findAll(pageable).map(PaymentMethodDTO::new);
    }
}
