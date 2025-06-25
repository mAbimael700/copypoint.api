package com.copypoint.api.domain.employee;

import com.copypoint.api.domain.employeerole.EmployeeRole;
import com.copypoint.api.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "employees")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(name = "registered_at")
    private LocalDateTime registeredAt;

    @Column(name = "last_modified_at")
    private LocalDateTime lastModifiedAt;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private EmployeeStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registered_by", referencedColumnName = "id")
    private User registeredBy;

    @Builder.Default
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EmployeeRole> employeeRoles = new ArrayList<>();
}