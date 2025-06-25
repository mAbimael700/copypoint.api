package com.copypoint.api.domain.employeerole;

import com.copypoint.api.domain.copypoint.Copypoint;
import com.copypoint.api.domain.employee.Employee;
import com.copypoint.api.domain.role.Role;
import com.copypoint.api.domain.store.Store;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "employee_roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", referencedColumnName = "id")
    @ToString.Exclude // Add this to break the circular reference
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "copypoint_id", referencedColumnName = "id")
    private Copypoint copypoint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", referencedColumnName = "id")
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", referencedColumnName = "id")
    private Role role;

    @Column(name = "added_at")
    private LocalDateTime addedAt;
}
