package com.copypoint.api.domain.profile;

import com.copypoint.api.domain.configuration.Configuration;
import com.copypoint.api.domain.material.Material;
import com.copypoint.api.domain.material.ProfileMaterial;
import com.copypoint.api.domain.sales.SaleProfile;
import com.copypoint.api.domain.service.Service;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "profiles")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String name;

    private String description;

    @Column(name = "unit_price")
    private Double unitPrice;

    private Boolean active;

    @Column(name = "last_modified_at")
    private LocalDateTime lastModifiedAt;

    @ManyToMany(mappedBy = "profiles", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<Service> services = new ArrayList<>();

    @OneToMany(mappedBy = "profile", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<SaleProfile> saleProfiles = new ArrayList<>();

    // Relación correcta: Un perfil puede tener muchos materiales
    @OneToMany(mappedBy = "profile", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<ProfileMaterial> profileMaterials = new ArrayList<>();

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "profile_configurations",
            joinColumns = @JoinColumn(name = "profile_id"),
            inverseJoinColumns = @JoinColumn(name = "configuration_id"))
    @Builder.Default
    private List<Configuration> configurations = new ArrayList<>();
}
