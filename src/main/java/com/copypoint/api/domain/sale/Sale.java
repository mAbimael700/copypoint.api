package com.copypoint.api.domain.sale;

import com.copypoint.api.domain.copypoint.Copypoint;
import com.copypoint.api.domain.payment.entity.Payment;
import com.copypoint.api.domain.paymentmethod.PaymentMethod;
import com.copypoint.api.domain.saleprofile.SaleProfile;
import com.copypoint.api.domain.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Sale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User userVendor;

    @ManyToOne
    @JoinColumn(name = "payment_method_id", referencedColumnName = "id")
    private PaymentMethod paymentMethod;

    @ManyToOne
    @JoinColumn(name = "copypoint_id", referencedColumnName = "id")
    private Copypoint copypoint;

    @Column(name = "total_sale")
    private Double total;

    @Column(length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    private SaleStatus status;

    private Double discount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Corregido: mappedBy debe apuntar al campo, no al ID
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "sale")
    @Builder.Default
    private List<SaleProfile> saleProfiles = new ArrayList<>();

    @OneToMany(mappedBy = "sale", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();
}
