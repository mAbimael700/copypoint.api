package com.copypoint.api.domain.employee.repository;

import com.copypoint.api.domain.copypoint.Copypoint;
import com.copypoint.api.domain.employee.Employee;
import com.copypoint.api.domain.employeerole.EmployeeRolePermissionProjection;
import com.copypoint.api.domain.store.Store;
import com.copypoint.api.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByUser(User user);

    @Query(value = """
    SELECT 
        er.id as employeeRoleId,
        er.employee_id as employeeId,
        er.copypoint_id as copypointId,
        er.store_id as storeId,
        er.role_id as roleId,
        er.added_at as addedAt,
        r.name as roleName,
        p.module_id as moduleId,
        m.name as moduleName,
        m.active as moduleActive
    FROM employee_roles er
    JOIN roles r ON er.role_id = r.id
    JOIN permissions p ON p.rol_id = r.id
    JOIN modules m ON p.module_id = m.id
    JOIN employees e ON er.employee_id = e.id
    WHERE e.user_id = :userId
    """, nativeQuery = true)
    List<EmployeeRolePermissionProjection> findEmployeeRolePermissionsByUser(@Param("userId") Long userId);

    // Agregar métodos para verificar duplicados
    boolean existsByUserAndEmployeeRoles_Store(User user, Store store);
    boolean existsByUserAndEmployeeRoles_Copypoint(User user, Copypoint copypoint);

    Optional<Employee> findByUserAndEmployeeRoles_Store(User user, Store store);

    Optional<Employee> findByUserAndEmployeeRoles_Copypoint(User user, Copypoint copypoint);
}
