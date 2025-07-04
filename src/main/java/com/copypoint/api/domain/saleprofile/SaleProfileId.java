package com.copypoint.api.domain.saleprofile;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class SaleProfileId implements Serializable {
    @Column(name = "sale_id")
    private Long saleId;
    @Column(name = "profile_id")
    private Long profileId;
    @Column(name = "service_id")
    private Long serviceId;
}
