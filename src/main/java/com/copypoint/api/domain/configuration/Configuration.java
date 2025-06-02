package com.copypoint.api.domain.configuration;

import com.copypoint.api.domain.copypoint.Copypoint;
import com.copypoint.api.domain.profile.Profile;
import com.copypoint.api.domain.store.Store;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "configurations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Configuration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;

    private String value;

    @Column(name = "cost_impact")
    private Double costImpact;

    private Boolean active;

    @ManyToMany(mappedBy = "configurations", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<Profile> profiles = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "store_id", referencedColumnName = "id")
    private Store store;
}
