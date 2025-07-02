package com.copypoint.api.infra.http.controller;

import com.copypoint.api.domain.paymentmethod.dto.PaymentMethodDTO;
import com.copypoint.api.domain.paymentmethod.service.PaymentMethodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payment-methods")
public class PaymentMethodController {
    @Autowired
    private PaymentMethodService paymentMethodService;

    @GetMapping
    public ResponseEntity<Page<PaymentMethodDTO>> getAll(Pageable pageable) {
        Page<PaymentMethodDTO> paymentMethods = paymentMethodService.getAll(pageable);
        return ResponseEntity.ok(paymentMethods);
    };
}
