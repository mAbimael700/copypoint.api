package com.copypoint.api.domain.payment;

import com.copypoint.api.domain.paymentmethod.PaymentMethod;
import com.copypoint.api.domain.sale.Sale;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sale_id", referencedColumnName = "id")
    private Sale sale;

    @ManyToOne
    @JoinColumn(name = "payment_method_id")
    private PaymentMethod paymentMethod;

    /**
     * ID de la intención de pago en la pasarela (checkout/session/intent)
     * - MercadoPago: Preference ID
     * - Stripe: Payment Intent ID / Checkout Session ID
     * - PayPal: Order ID
     * - Square: Payment ID (antes del checkout)
     * - Conekta: Order ID
     */
    @Column(name = "gateway_intent_id")
    private String gatewayIntentId;

    /**
     * ID del pago completado/procesado en la pasarela
     * - MercadoPago: Payment ID
     * - Stripe: Payment Intent ID (confirmado) / Charge ID
     * - PayPal: Capture ID / Payment ID
     * - Square: Payment ID (después del checkout)
     * - Conekta: Charge ID
     */
    @Column(name = "gateway_payment_id")
    private String gatewayPaymentId;

    /**
     * ID de la transacción a nivel bancario/procesador (si está disponible)
     * - Número de autorización bancaria
     * - Transaction ID del procesador
     * - Reference number del banco
     */
    @Column(name = "gateway_transaction_id")
    private String gatewayTransactionId;

    /**
     * @deprecated Usar gatewayIntentId y gatewayPaymentId en su lugar
     * Mantenido temporalmente para compatibilidad
     */
    @Deprecated
    @Column(name = "gateway_id")
    private String gatewayId;

    private Double amount;

    @Column(length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private PaymentStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "modified_at")
    private LocalDateTime modifiedAt;



    // Métodos de conveniencia
    /**
     * Retorna el ID más relevante según el estado del pago
     */
    public String getCurrentGatewayId() {
        if (gatewayPaymentId != null && !gatewayPaymentId.trim().isEmpty()) {
            return gatewayPaymentId;
        }
        if (gatewayIntentId != null && !gatewayIntentId.trim().isEmpty()) {
            return gatewayIntentId;
        }
        return gatewayId; // fallback legacy
    }

    /**
     * Verifica si el pago tiene un Payment ID (pago completado)
     */
    public boolean hasGatewayPaymentId() {
        return gatewayPaymentId != null && !gatewayPaymentId.trim().isEmpty();
    }

    /**
     * Verifica si el pago tiene un Intent ID (checkout creado)
     */
    public boolean hasGatewayIntentId() {
        return gatewayIntentId != null && !gatewayIntentId.trim().isEmpty();
    }

    /**
     * Verifica si el pago tiene un Transaction ID (procesado a nivel bancario)
     */
    public boolean hasGatewayTransactionId() {
        return gatewayTransactionId != null && !gatewayTransactionId.trim().isEmpty();
    }

    /**
     * Obtiene el nombre del tipo de pasarela basado en el PaymentMethod
     */
    public String getGatewayType() {
        if (paymentMethod != null && paymentMethod.getGateway() != null) {
            return paymentMethod.getGateway().toLowerCase();
        }
        return "unknown";
    }
}
