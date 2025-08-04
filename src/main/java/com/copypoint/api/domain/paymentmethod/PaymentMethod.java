package com.copypoint.api.domain.paymentmethod;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment_methods")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String description;

    /**
     * Pasarela de pago que maneja este método
     * Ejemplo: "mercadopago", "stripe", "paypal", "square", "conekta"
     */
    @Column(name = "gateway")
    private String gateway;

    /**
     * Tipo específico del método en la pasarela
     * - MercadoPago: "credit_card", "debit_card", "digital_wallet", "bank_transfer"
     * - Stripe: "card", "sepa_debit", "ideal", "sofort"
     * - PayPal: "paypal", "venmo", "paylater"
     */
    @Column(name = "gateway_method_type")
    private String gatewayMethodType;

    /**
     * Configuración adicional en formato JSON
     * Puede incluir configuraciones específicas de la pasarela
     */
    @Column(name = "configuration", columnDefinition = "json")
    private String configuration;

    @Column(name = "is_active")
    private Boolean isActive = true;

    // Métodos de conveniencia
    public boolean isMercadoPago() {
        return "mercadopago".equalsIgnoreCase(gateway);
    }

    public boolean isStripe() {
        return "stripe".equalsIgnoreCase(gateway);
    }

    public boolean isPayPal() {
        return "paypal".equalsIgnoreCase(gateway);
    }

    public boolean isSquare() {
        return "square".equalsIgnoreCase(gateway);
    }

    public boolean isConekta() {
        return "conekta".equalsIgnoreCase(gateway);
    }
}
