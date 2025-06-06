package com.copypoint.api.domain.modules;

import com.copypoint.api.domain.permission.Permission;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "modules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"permissions", "subModules", "parentModule"})
@EqualsAndHashCode(exclude = {"permissions", "subModules", "parentModule"})
public class Module {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    // Relación con permisos - Sin cascade ALL para evitar eliminaciones accidentales
    @OneToMany(mappedBy = "module",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            fetch = FetchType.LAZY,
            orphanRemoval = true
    )
    @Builder.Default
    private List<Permission> permissions = new ArrayList<>();

    // Relación auto-referencial para módulos padre/hijo
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "parent_module_id")
    private Module parentModule;

    // Cambié el nombre para ser más claro: subModules en lugar de modules
    @OneToMany(mappedBy = "parentModule", fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Builder.Default
    private List<Module> subModules = new ArrayList<>();

    // Métodos de conveniencia para mantener la consistencia bidireccional
    public void addSubModule(Module subModule) {
        if (subModule != null) {
            subModules.add(subModule);
            subModule.setParentModule(this);
        }
    }

    public void removeSubModule(Module subModule) {
        if (subModule != null) {
            subModules.remove(subModule);
            subModule.setParentModule(null);
        }
    }

    public void addPermission(Permission permission) {
        if (permission != null) {
            permissions.add(permission);
            permission.setModule(this);
        }
    }

    public void removePermission(Permission permission) {
        if (permission != null) {
            permissions.remove(permission);
            permission.setModule(null);
        }
    }

    // Método utilitario para verificar si es un módulo raíz
    public boolean isRootModule() {
        return parentModule == null;
    }

    // Método utilitario para verificar si tiene submódulos
    public boolean hasSubModules() {
        return subModules != null && !subModules.isEmpty();
    }
}
