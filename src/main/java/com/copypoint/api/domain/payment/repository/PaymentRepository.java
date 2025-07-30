package com.copypoint.api.domain.payment.repository;

import com.copypoint.api.domain.payment.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByGatewayId(String paymentId);

    Page<Payment> findBySale_CopypointId(Long copypointId, Pageable pageable);

    Page<Payment> findBySaleId(Long saleId, Pageable pageable);

    @Query("SELECT p FROM Payment p WHERE p.sale.copypoint.id = :copypointId")
    Page<Payment> findPaymentsByCopypointId(@Param("copypointId") Long copypointId, Pageable pageable);
}
