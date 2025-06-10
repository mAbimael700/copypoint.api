package com.copypoint.api.infra.http.authorization.dto;

import com.copypoint.api.infra.http.context.ContextType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleInfo {
    /**
     * ID del rol
     */
    private Long roleId;

    /**
     * Nombre del rol (ej: "STORE_ADMIN", "CASHIER", "MANAGER")
     */
    private String roleName;

    /**
     * Tipo de contexto donde aplica este rol
     */
    private ContextType contextType;

    /**
     * ID del contexto específico (Store ID o Copypoint ID)
     */
    private Long contextId;

    /**
     * Nombre del contexto (para logs/debugging)
     */
    private String contextName;

    /**
     * Set de códigos de módulos permitidos por este rol
     */
    @Builder.Default
    private Set<String> modulePermissions = Set.of();

    /**
     * Fecha cuando se asignó el rol
     */
    private LocalDateTime assignedAt;

    /**
     * Si el rol está activo
     */
    @Builder.Default
    private Boolean active = true;
}
