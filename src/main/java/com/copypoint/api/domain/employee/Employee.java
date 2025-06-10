package com.copypoint.api.domain.employee;

import com.copypoint.api.domain.copypoint.Copypoint;
import com.copypoint.api.domain.employeeRole.EmployeeRole;
import com.copypoint.api.domain.role.Role;
import com.copypoint.api.domain.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "employees")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {
    @EmbeddedId
    private EmployeeId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("copypointId")
    @JoinColumn(name = "copypoint_id", referencedColumnName = "id")
    private Copypoint copypoint;

    @ManyToOne
    @MapsId("userId")
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
    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY)
    private List<EmployeeRole> employeeRoles = new ArrayList<>();
}