package com.copypoint.api.domain.employee.service;

import com.copypoint.api.domain.copypoint.Copypoint;
import com.copypoint.api.domain.employee.Employee;
import com.copypoint.api.domain.employee.repository.EmployeeRepository;
import com.copypoint.api.domain.employee.EmployeeStatus;
import com.copypoint.api.domain.employee.dto.EmployeeDTO;
import com.copypoint.api.domain.employeerole.EmployeeRole;
import com.copypoint.api.domain.employeerole.EmployeeRoleRepository;
import com.copypoint.api.domain.role.Role;
import com.copypoint.api.domain.role.RoleRepository;
import com.copypoint.api.domain.role.RoleType;
import com.copypoint.api.domain.store.Store;
import com.copypoint.api.domain.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeRoleRepository employeeRoleRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Transactional
    public EmployeeDTO saveEmployee(
            User user,
            Store store,
            Copypoint copypoint,
            RoleType roleType) {

        // Validaciones
        if (user == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo");
        }

        if (store == null && copypoint == null) {
            throw new IllegalArgumentException("Debe especificar una tienda o un copypoint");
        }

        Optional<Role> role = roleRepository.findByName(roleType.getCode());

        if (role.isEmpty()) {
            throw new RuntimeException("No se encontró el rol: " + roleType.getCode());
        }

        // Buscar si ya existe un empleado para este usuario
        Optional<Employee> existingEmployee = findExistingEmployee(user, store, copypoint);

        if (existingEmployee.isPresent()) {
            // El empleado ya existe, verificar si tiene el rol específico
            return handleExistingEmployee(existingEmployee.get(), role.get(), store, copypoint);
        } else {
            // El empleado no existe, crear uno nuevo
            return createNewEmployee(user, role.get(), store, copypoint);
        }

    }


    private Optional<Employee> findExistingEmployee(User user, Store store, Copypoint copypoint) {
        if (store != null) {
            return employeeRepository.findByUserAndEmployeeRoles_Store(user, store);
        } else if (copypoint != null) {
            return employeeRepository.findByUserAndEmployeeRoles_Copypoint(user, copypoint);
        }
        return Optional.empty();
    }

    private EmployeeDTO handleExistingEmployee(Employee employee, Role role, Store store, Copypoint copypoint) {
        // Verificar si el empleado ya tiene este rol específico en esta tienda/copypoint
        boolean hasRole = employee.getEmployeeRoles().stream()
                .anyMatch(er -> er.getRole().equals(role) &&
                        ((store != null && store.equals(er.getStore())) ||
                                (copypoint != null && copypoint.equals(er.getCopypoint()))));

        if (hasRole) {
            // Ya tiene el rol, no hacer nada, retornar el empleado existente
            return new EmployeeDTO(employee);
        } else {
            // No tiene el rol, agregarlo
            EmployeeRole newEmployeeRole = EmployeeRole.builder()
                    .role(role)
                    .employee(employee)
                    .addedAt(LocalDateTime.now())
                    .build();

            if (store != null) {
                newEmployeeRole.setStore(store);
            }
            if (copypoint != null) {
                newEmployeeRole.setCopypoint(copypoint);
            }

            // Agregar el nuevo rol a la lista del empleado
            employee.getEmployeeRoles().add(newEmployeeRole);
            employee.setLastModifiedAt(LocalDateTime.now());

            // Guardar los cambios
            Employee savedEmployee = employeeRepository.save(employee);
            return new EmployeeDTO(savedEmployee);
        }
    }

    private EmployeeDTO createNewEmployee(User user, Role role, Store store, Copypoint copypoint) {
        // Crear empleado con lista mutable
        Employee newEmployee = Employee.builder()
                .user(user)
                .status(EmployeeStatus.ACTIVE)
                .registeredAt(LocalDateTime.now())
                .lastModifiedAt(LocalDateTime.now())
                .employeeRoles(new ArrayList<>())
                .build();

        // Crear rol del empleado
        EmployeeRole employeeRole = EmployeeRole.builder()
                .role(role)
                .employee(newEmployee)
                .addedAt(LocalDateTime.now())
                .build();

        if (store != null) {
            employeeRole.setStore(store);
        }
        if (copypoint != null) {
            employeeRole.setCopypoint(copypoint);
        }

        // Agregar rol a la lista del empleado
        newEmployee.getEmployeeRoles().add(employeeRole);

        // Guardar el nuevo empleado
        Employee savedEmployee = employeeRepository.save(newEmployee);
        return new EmployeeDTO(savedEmployee);
    }

    // Método auxiliar para verificar si existe un empleado (mantener compatibilidad)
    private boolean employeeAlreadyExists(User user, Store store, Copypoint copypoint) {
        return findExistingEmployee(user, store, copypoint).isPresent();
    }

}
