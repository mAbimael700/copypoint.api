package com.copypoint.api.domain.administrator;

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
public class AdministratorId implements Serializable {
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "store_id")
    private Long storeId;
}
