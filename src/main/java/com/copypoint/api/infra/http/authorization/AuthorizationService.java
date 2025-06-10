package com.copypoint.api.infra.http.authorization;

import com.copypoint.api.domain.role.Role;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class AuthorizationService {
    // Obtener roles del usuario en contexto específico
    public List<Role> getUserRolesInStore(Long userId, Long storeId);
    public List<Role> getUserRolesInCopypoint(Long userId, Long copypointId);

    // Obtener permisos desde roles
    public Set<String> getPermissionsFromRoles(List<Role> roles);

    // Validar acceso a módulo
    public boolean hasPermissionForModule(Set<String> permissions, String moduleCode);
}
