package com.copypoint.api.domain.payment.repository;

import com.copypoint.api.domain.payment.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByGatewayId(String paymentId);

    Page<Payment> findBySale_CopypointId(Long copypointId, Pageable pageable);

    Page<Payment> findBySaleId(Long saleId, Pageable pageable);

    @Query("SELECT p FROM Payment p WHERE p.sale.copypoint.id = :copypointId")
    Page<Payment> findPaymentsByCopypointId(@Param("copypointId") Long copypointId, Pageable pageable);

    // Métodos específicos por tipo de ID
    Optional<Payment> findByGatewayIntentId(String gatewayIntentId);
    Optional<Payment> findByGatewayPaymentId(String gatewayPaymentId);
    Optional<Payment> findByGatewayTransactionId(String gatewayTransactionId);


    /**
     * Busca un payment por cualquier ID de pasarela
     * Útil para webhooks donde no sabemos qué tipo de ID nos llega
     */
    @Query("""
        SELECT p FROM Payment p
        WHERE p.gatewayIntentId = :id
           OR p.gatewayPaymentId = :id
           OR p.gatewayTransactionId = :id
           OR p.gatewayId = :id
        """)
    Optional<Payment> findByAnyGatewayId(@Param("id") String id);

    /**
     * Busca payments por pasarela específica
     */
    @Query("""
        SELECT p FROM Payment p 
        JOIN p.paymentMethod pm 
        WHERE LOWER(pm.gateway) = LOWER(:gateway)
        """)
    Page<Payment> findByGateway(@Param("gateway") String gateway, Pageable pageable);

    /**
     * Busca payments que tienen Intent ID pero no Payment ID
     * (pagos iniciados pero no completados)
     */
    @Query("""
        SELECT p FROM Payment p 
        WHERE p.gatewayIntentId IS NOT NULL 
          AND (p.gatewayPaymentId IS NULL OR p.gatewayPaymentId = '')
        """)
    Page<Payment> findPendingPaymentsWithIntent(Pageable pageable);

    /**
     * Busca payments completados (con Payment ID)
     */
    @Query("""
        SELECT p FROM Payment p 
        WHERE p.gatewayPaymentId IS NOT NULL 
          AND p.gatewayPaymentId != ''
        """)
    Page<Payment> findCompletedPayments(Pageable pageable);

    /**
     * Busca payments por pasarela específica y estado
     */
    @Query("""
        SELECT p FROM Payment p 
        JOIN p.paymentMethod pm 
        WHERE LOWER(pm.gateway) = LOWER(:gateway)
          AND p.status = :status
        """)
    List<Payment> findByGatewayAndStatus(@Param("gateway") String gateway,
                                         @Param("status") com.copypoint.api.domain.payment.PaymentStatus status);

}
