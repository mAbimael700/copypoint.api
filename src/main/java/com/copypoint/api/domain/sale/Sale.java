package com.copypoint.api.domain.sale;

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

    @Column(name = "total_sale")
    private Double total;

    @Column(length = 3)
    private String currency;

    @Column(length = 50)
    private String status;

    private Double discount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Corregido: mappedBy debe apuntar al campo, no al ID
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "sale")
    @Builder.Default
    private List<SaleProfile> saleProfiles = new ArrayList<>();
}
