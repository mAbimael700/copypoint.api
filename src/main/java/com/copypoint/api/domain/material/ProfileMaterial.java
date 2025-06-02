package com.copypoint.api.domain.material;

import com.copypoint.api.domain.profile.Profile;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "profile_materials")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileMaterial {

    @EmbeddedId
    private ProfileMaterialId id;

    @ManyToOne
    @MapsId("materialId")
    @JoinColumn(name = "material_id")
    private Material material;

    @ManyToOne
    @MapsId("profileId")
    @JoinColumn(name = "profile_id")
    private Profile profile;

    private Integer quantity;

    private Boolean obligatory;

    @Column(length = 500)
    private String notes;

}
