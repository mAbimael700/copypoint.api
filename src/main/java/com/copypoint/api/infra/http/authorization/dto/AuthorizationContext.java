package com.copypoint.api.infra.http.authorization.dto;

import com.copypoint.api.infra.http.context.ContextType;
import com.copypoint.api.infra.http.enpointModule.ModuleCode;
import com.copypoint.api.infra.http.permission.PermissionAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Representa el contexto de autorización para una request específica
 * Contiene toda la información necesaria para validar permisos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizationContext {
    /**
     * ID del usuario que hace la request
     */
    private Long userId;

    /**
     * Email del usuario (para logs y debugging)
     */
    private String userEmail;

    /**
     * Tipo de contexto (STORE, COPYPOINT, GLOBAL)
     */
    private ContextType contextType;

    /**
     * ID del contexto específico (Store ID o Copypoint ID)
     */
    private Long contextId;

    /**
     * Set de códigos de módulos a los que el usuario tiene acceso
     * en el contexto actual
     */
    @Builder.Default
    private Set<String> allowedModules = Set.of();

    /**
     * Acción específica que se intenta realizar (opcional)
     */
    private PermissionAction action;

    /**
     * Información adicional para debugging
     */
    private String requestPath;
    private String httpMethod;

    /**
     * Verifica si el usuario tiene acceso a un módulo específico
     */
    public boolean hasAccessToModule(String moduleCode) {
        return allowedModules.contains(moduleCode);
    }

    /**
     * Verifica si el usuario tiene acceso a cualquiera de los módulos especificados
     */
    public boolean hasAccessToAnyModule(Set<String> moduleCodes) {
        return moduleCodes.stream().anyMatch(this::hasAccessToModule);
    }
}
