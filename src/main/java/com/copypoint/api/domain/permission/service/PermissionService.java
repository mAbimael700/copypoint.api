package com.copypoint.api.domain.permission.service;

import com.copypoint.api.domain.employee.Employee;
import com.copypoint.api.domain.employee.repository.EmployeeRepository;
import com.copypoint.api.domain.employeerole.EmployeeRolePermissionProjection;
import com.copypoint.api.domain.module.ModuleType;
import com.copypoint.api.domain.pathcontext.PathContext;
import com.copypoint.api.domain.user.User;
import com.copypoint.api.infra.http.authorization.ModuleEndpointMapping;
import com.copypoint.api.infra.http.userprincipal.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PermissionService {

    @Autowired
    private EmployeeRepository employeeRepository;


    @Autowired
    private ModuleEndpointMapping moduleEndpointMapping;


    // Método público sin @Transactional
    public boolean hasPermission(UserPrincipal userPrincipal, String method, String path) {

        Optional<ModuleType> moduleOpt = moduleEndpointMapping.findModuleForEndpoint(method, path);

        if (moduleOpt.isEmpty()) {
            return false;
        }

        ModuleType moduleName = moduleOpt.get();
        PathContext pathContext = PathContext.extractPathContext(path);

        // Verificar si el usuario tiene el módulo
        if (!userPrincipal.hasModule(moduleName.getCode())) {
            return false;
        }

        // Si no hay contexto específico, el usuario ya tiene acceso
        if (!pathContext.hasContext()) {
            return true;
        }

        // Para verificación contextual, necesitamos consultar la base de datos
        // pero solo una vez y solo para este usuario
        return checkContextualPermissionWithTransaction(userPrincipal.getUser(), moduleName, pathContext);
    }

    @Transactional(readOnly = true)
    private boolean checkContextualPermissionWithTransaction(User user, ModuleType moduleName, PathContext pathContext) {
        List<EmployeeRolePermissionProjection> rolePermissions =
                employeeRepository.findEmployeeRolePermissionsByUser(user.getId());

        boolean hasAccess = rolePermissions.stream()
                .anyMatch(rp ->
                        hasModulePermission(rp, moduleName.getCode()) &&
                                hasContextualPermission(rp, pathContext));

        System.out.println("Has access: " + hasAccess);
        return hasAccess;
    }

    private boolean hasModulePermission(EmployeeRolePermissionProjection rolePermission, String moduleName) {
        return rolePermission.getModuleName().equals(moduleName) &&
                Boolean.TRUE.equals(rolePermission.getModuleActive());
    }

    private boolean hasContextualPermission(EmployeeRolePermissionProjection rolePermission, PathContext context) {
        if (!context.hasContext()) {
            return true;
        }

        if (context.getStoreId() != null) {
            return Objects.equals(rolePermission.getStoreId(), context.getStoreId());
        }

        if (context.getCopypointId() != null) {
            return Objects.equals(rolePermission.getCopypointId(), context.getCopypointId());
        }

        return true;
    }


    /**
     * Obtiene todos los módulos a los que tiene acceso un usuario
     */
    public Set<String> getUserModules(User user) {
        List<Employee> employees = employeeRepository.findByUser(user);

        return employees.stream()
                .flatMap(employee -> employee.getEmployeeRoles().stream())
                .flatMap(employeeRole -> employeeRole.getRole().getPermissions().stream())
                .filter(permission -> permission.getModule().getActive())
                .map(permission -> permission.getModule().getName())
                .collect(Collectors.toSet());
    }

    /**
     * Obtiene todos los roles que tiene un usuario
     */
    @Transactional(readOnly = true)
    public Set<String> getUserRoles(User user) {
        List<EmployeeRolePermissionProjection> rolePermissions =
                employeeRepository.findEmployeeRolePermissionsByUser(user.getId());

        return rolePermissions.stream()
                .map(EmployeeRolePermissionProjection::getRoleName)
                .collect(Collectors.toSet());
    }

    /**
     * Obtiene la información completa de permisos de un usuario
     */
    @Transactional(readOnly = true)
    public UserPermissionInfo getUserPermissionInfo(User user) {
        Set<String> modules = getUserModules(user);
        Set<String> roles = getUserRoles(user);

        return new UserPermissionInfo(modules, roles);
    }

    // Clase interna para encapsular la información de permisos
    public static class UserPermissionInfo {
        private final Set<String> modules;
        private final Set<String> roles;

        public UserPermissionInfo(Set<String> modules, Set<String> roles) {
            this.modules = modules;
            this.roles = roles;
        }

        public Set<String> getModules() { return modules; }
        public Set<String> getRoles() { return roles; }
    }



    /**
     * Verifica si el usuario tiene un rol específico para un copypoint
     */
    @Transactional(readOnly = true)
    public boolean hasRoleForCopypoint(User user, String roleName, Long copypointId) {
        List<EmployeeRolePermissionProjection> rolePermissions =
                employeeRepository.findEmployeeRolePermissionsByUser(user.getId());

        return rolePermissions.stream()
                .anyMatch(rp ->
                        rp.getRoleName().equals(roleName) &&
                                Objects.equals(rp.getCopypointId(), copypointId));
    }

    /**
     * Obtiene el rol principal del usuario para un copypoint específico
     */
    @Transactional(readOnly = true)
    public Optional<String> getUserRoleForCopypoint(User user, Long copypointId) {
        List<EmployeeRolePermissionProjection> rolePermissions =
                employeeRepository.findEmployeeRolePermissionsByUser(user.getId());

        return rolePermissions.stream()
                .filter(rp -> Objects.equals(rp.getCopypointId(), copypointId))
                .map(EmployeeRolePermissionProjection::getRoleName)
                .findFirst();
    }

    /**
     * Obtiene todos los roles del usuario para un copypoint específico
     */
    @Transactional(readOnly = true)
    public Set<String> getUserRolesForCopypoint(User user, Long copypointId) {
        List<EmployeeRolePermissionProjection> rolePermissions =
                employeeRepository.findEmployeeRolePermissionsByUser(user.getId());

        return rolePermissions.stream()
                .filter(rp -> Objects.equals(rp.getCopypointId(), copypointId))
                .map(EmployeeRolePermissionProjection::getRoleName)
                .collect(Collectors.toSet());
    }

    /**
     * Verifica si el usuario tiene acceso a un copypoint específico
     */
    @Transactional(readOnly = true)
    public boolean hasAccessToCopypoint(User user, Long copypointId) {
        List<EmployeeRolePermissionProjection> rolePermissions =
                employeeRepository.findEmployeeRolePermissionsByUser(user.getId());

        return rolePermissions.stream()
                .anyMatch(rp -> Objects.equals(rp.getCopypointId(), copypointId));
    }

    /**
     * Obtiene información contextual completa del usuario para un copypoint
     */
    @Transactional(readOnly = true)
    public CopypointAccessInfo getUserCopypointAccess(User user, Long copypointId) {
        List<EmployeeRolePermissionProjection> rolePermissions =
                employeeRepository.findEmployeeRolePermissionsByUser(user.getId());

        Set<String> roles = rolePermissions.stream()
                .filter(rp -> Objects.equals(rp.getCopypointId(), copypointId))
                .map(EmployeeRolePermissionProjection::getRoleName)
                .collect(Collectors.toSet());

        Set<String> modules = rolePermissions.stream()
                .filter(rp -> Objects.equals(rp.getCopypointId(), copypointId))
                .filter(rp -> Boolean.TRUE.equals(rp.getModuleActive()))
                .map(EmployeeRolePermissionProjection::getModuleName)
                .collect(Collectors.toSet());

        return new CopypointAccessInfo(copypointId, roles, modules, !roles.isEmpty());
    }

    // Clase para encapsular información de acceso a copypoint
    public static class CopypointAccessInfo {
        private final Long copypointId;
        private final Set<String> roles;
        private final Set<String> modules;
        private final boolean hasAccess;

        public CopypointAccessInfo(Long copypointId, Set<String> roles, Set<String> modules, boolean hasAccess) {
            this.copypointId = copypointId;
            this.roles = roles;
            this.modules = modules;
            this.hasAccess = hasAccess;
        }

        // Getters
        public Long getCopypointId() { return copypointId; }
        public Set<String> getRoles() { return roles; }
        public Set<String> getModules() { return modules; }
        public boolean hasAccess() { return hasAccess; }

        // Métodos de conveniencia
        public boolean hasRole(String roleName) { return roles.contains(roleName); }
        public boolean hasModule(String moduleName) { return modules.contains(moduleName); }
        public boolean hasAnyRole(String... roleNames) {
            return roles.stream().anyMatch(role -> Arrays.asList(roleNames).contains(role));
        }
    }
}





