package com.copypoint.api.domain.permission;

import com.copypoint.api.domain.modules.Module;
import com.copypoint.api.domain.role.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {
    @EmbeddedId
    private PermissionId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roleId")
    @JoinColumn(name = "rol_id", referencedColumnName = "id")
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("moduleId")
    @JoinColumn(name = "module_id", referencedColumnName = "id")
    private Module module;
}
