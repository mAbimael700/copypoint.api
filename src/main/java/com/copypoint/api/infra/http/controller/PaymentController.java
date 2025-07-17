package com.copypoint.api.infra.http.controller;

import com.copypoint.api.domain.payment.dto.PaymentResponse;
import com.copypoint.api.domain.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @GetMapping
    public ResponseEntity<Page<PaymentResponse>> getByCopypoint(
            @RequestParam Long copypointId,
            Pageable pageable) {

        Page<PaymentResponse> payments = paymentService
                .getPaymentsByCopypoint(copypointId, pageable).map(PaymentResponse::new);

        return ResponseEntity.ok(payments);
    }

    @GetMapping
    public ResponseEntity<Page<PaymentResponse>> getBySale(
            @RequestParam Long saleId,
            Pageable pageable) {

        Page<PaymentResponse> payments = paymentService
                .getPaymentsBySale(saleId, pageable).map(PaymentResponse::new);

        return ResponseEntity.ok(payments);
    }



}
