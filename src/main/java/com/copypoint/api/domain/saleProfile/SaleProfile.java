package com.copypoint.api.domain.saleProfile;

import com.copypoint.api.domain.attachment.Attachment;
import com.copypoint.api.domain.profile.Profile;
import com.copypoint.api.domain.sale.Sale;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sale_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleProfile {
    @EmbeddedId
    private SaleProfilesId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("saleId")
    @JoinColumn(name = "sale_id", referencedColumnName = "id")
    private Sale sale;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("profileId")
    @JoinColumn(name = "profile_id", referencedColumnName = "id")
    private Profile profile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", referencedColumnName = "id")
    private Attachment attachment;

    @Column(name = "unit_price")
    private Double unitPrice;

    private Integer quantity;

    private Double subtotal;
}
