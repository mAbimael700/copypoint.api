package com.copypoint.api.domain.client;

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
public class ClientId implements Serializable {
    @Column(name = "client_id")
    private Long clientId;

    @Column(name = "store_id")
    private Long storeId;
}
