package com.copypoint.api.domain.employees;

import com.copypoint.api.domain.copypoint.Copypoint;
import com.copypoint.api.domain.role.Role;
import com.copypoint.api.domain.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "employees")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {
    @EmbeddedId
    private EmployeeId id;

    @Column(name = "registered_at")
    private LocalDateTime registeredAt;

    @Column(name = "last_modified_at")
    private LocalDateTime lastModifiedAt;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private EmployeeStatus status;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "registered_by",referencedColumnName = "id")
    private User registeredBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roleId")
    @JoinColumn(name = "role_id", referencedColumnName = "id")
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("copypointId")
    @JoinColumn(name = "copypoint_id", referencedColumnName = "id")
    private Copypoint copypoint;
}