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

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roleId")
    @JoinColumn(name = "role_id", referencedColumnName = "id")
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("copypointId")
    @JoinColumn(name = "id", referencedColumnName = "id")
    private Copypoint copypoint;

}

