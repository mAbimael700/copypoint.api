package com.copypoint.api.domain.paymentattempt.repository;

import com.copypoint.api.domain.paymentattempt.entity.PaymentAttempt;
import com.copypoint.api.domain.paymentattempt.entity.PaymentAttemptStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt, Long> {
    /* Encuentra todos los intentos de pago para un payment específico, ordenados por fecha de creación descendente
     */
    List<PaymentAttempt> findByPaymentReferenceIdOrderByCreatedAtDesc(Long paymentId);

    /**
     * Encuentra intentos de pago por estado, ordenados por fecha de creación descendente
     */
    List<PaymentAttempt> findByStatusOrderByCreatedAtDesc(PaymentAttemptStatus status);

    /**
     * Cuenta el número de intentos para un payment específico
     */
    long countByPaymentReferenceId(Long paymentId);

    /**
     * Encuentra intentos de pago anteriores a una fecha específica
     */
    List<PaymentAttempt> findByCreatedAtBefore(LocalDateTime date);

    /**
     * Encuentra intentos de pago entre fechas específicas
     */
    List<PaymentAttempt> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Encuentra intentos de pago por payment ID y estado
     */
    List<PaymentAttempt> findByPaymentReferenceIdAndStatusOrderByCreatedAtDesc(Long paymentId, PaymentAttemptStatus status);

    /**
     * Encuentra el último intento de pago para un payment específico
     */
    @Query("SELECT pa FROM PaymentAttempt pa WHERE pa.paymentReference.id = :paymentId ORDER BY pa.createdAt DESC LIMIT 1")
    PaymentAttempt findLatestByPaymentId(@Param("paymentId") Long paymentId);

    /**
     * Encuentra intentos de pago fallidos para un payment específico
     */
    @Query("SELECT pa FROM PaymentAttempt pa WHERE pa.paymentReference.id = :paymentId AND pa.status = 'FAILED' ORDER BY pa.createdAt DESC")
    List<PaymentAttempt> findFailedAttemptsByPaymentId(@Param("paymentId") Long paymentId);

    /**
     * Cuenta intentos de pago por estado en un rango de fechas
     */
    @Query("SELECT COUNT(pa) FROM PaymentAttempt pa WHERE pa.status = :status AND pa.createdAt BETWEEN :startDate AND :endDate")
    long countByStatusAndCreatedAtBetween(@Param("status") PaymentAttemptStatus status,
                                          @Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    /**
     * Encuentra intentos de pago con códigos de error específicos
     */
    List<PaymentAttempt> findByErrorCodeIsNotNullOrderByCreatedAtDesc();

    /**
     * Encuentra intentos de pago por código de error específico
     */
    List<PaymentAttempt> findByErrorCodeOrderByCreatedAtDesc(String errorCode);

    Optional<PaymentAttempt> findTopByPaymentReferenceIdAndStatusOrderByCreatedAtDesc(
            Long paymentId, PaymentAttemptStatus status);

    // Nuevos métodos para el monitoreo
    List<PaymentAttempt> findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime since);

    List<PaymentAttempt> findTop10ByGatewayResponseIsNotNullOrderByCreatedAtDesc();

    /**
     * Encuentra el último intento de pago para un payment específico usando el campo correcto
     */
    @Query("SELECT pa FROM PaymentAttempt pa WHERE pa.paymentReference.id = :paymentId ORDER BY pa.createdAt DESC LIMIT 1")
    Optional<PaymentAttempt> findTopByPaymentReferenceIdOrderByCreatedAtDesc(@Param("paymentId") Long paymentId);

    @Query(value = """
        SELECT pa.status,
               COUNT(pa.id) as attempt_count
        FROM payment_attempts pa 
        WHERE pa.created_at BETWEEN :startDate AND :endDate
        GROUP BY pa.status
        ORDER BY attempt_count DESC
        """, nativeQuery = true)
    List<Object[]> findAttemptsByStatus(@Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);
}
