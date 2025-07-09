package com.copypoint.api.domain.payment.repository;

import com.copypoint.api.domain.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByGatewayId(String paymentId);
}
