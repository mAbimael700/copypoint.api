package com.copypoint.api.domain.service;

import com.copypoint.api.domain.copypoint.Copypoint;
import com.copypoint.api.domain.profile.Profile;
import com.copypoint.api.domain.saleprofile.SaleProfile;
import com.copypoint.api.domain.store.Store;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "services")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(exclude = {"profiles"})
public class Service {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 70)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", referencedColumnName = "id")
    private Store store;

    @ManyToMany(mappedBy = "services", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Profile> profiles = new ArrayList<>();


    @OneToMany(mappedBy = "service", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<SaleProfile> saleProfiles = new ArrayList<>();

    private Boolean active;
}
