package com.copypoint.api.domain.paymentAttempt;

import com.copypoint.api.domain.payment.Payment;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_attempts")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment paymentReference;

    @Enumerated(EnumType.STRING)
    private PaymentAttemptStatus status;

    @Column(name = "error_code")
    private String errorCode;

    @Column(name = "gateway_response", columnDefinition = "json")
    private String gatewayResponse;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;
}
