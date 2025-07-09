package com.copypoint.api.domain.payment.repository;

import com.copypoint.api.domain.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

}
