package com.copypoint.api.domain.paymentattempt.repository;

import com.copypoint.api.domain.paymentattempt.PaymentAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt, Long> {
}
