package com.copypoint.api.domain.permission;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PermissionId implements Serializable {

    @Column(name = "rol_id")
    private Long roleId;

    @Column(name = "module_id")
    private Long moduleId;

}
