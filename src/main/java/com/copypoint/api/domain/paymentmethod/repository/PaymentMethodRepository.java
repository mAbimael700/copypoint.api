package com.copypoint.api.domain.paymentmethod.repository;

import com.copypoint.api.domain.paymentmethod.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {
    Optional<PaymentMethod> findByDescription(String paymentMethod);
}
