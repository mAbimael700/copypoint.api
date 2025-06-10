package com.copypoint.api.domain.role;

import com.copypoint.api.domain.administrator.Administrator;
import com.copypoint.api.domain.employee.Employee;
import com.copypoint.api.domain.permission.Permission;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"permissions"})
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20)
    private String name;

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Permission> permissions;
}
