package com.copypoint.api.domain.material;

import com.copypoint.api.domain.profile.Profile;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "materials")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Material {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String name;

    private String description;

    @Column(name = "unit_base", length = 20)
    private String unitBase;

    @Column(name = "unit_purchase", length = 20)
    private String unitPurchase;

    @Column(name = "unit_price")
    private Double unitPrice;

    @Column(name = "min_stock")
    private Integer minStock;

    private Integer stock;

    private Boolean active;

    // Relación correcta: Un material puede estar en muchos perfiles
    @OneToMany(mappedBy = "material", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<ProfileMaterial> profileMaterials = new ArrayList<>();
}
