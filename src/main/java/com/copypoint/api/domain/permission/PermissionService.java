package com.copypoint.api.domain.permission;

import com.copypoint.api.domain.employee.Employee;
import com.copypoint.api.domain.employee.EmployeeRepository;
import com.copypoint.api.domain.employeerole.EmployeeRolePermissionProjection;
import com.copypoint.api.domain.modules.ModuleType;
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
}





