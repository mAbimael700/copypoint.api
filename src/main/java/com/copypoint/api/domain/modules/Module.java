package com.copypoint.api.domain.modules;

import com.copypoint.api.domain.permission.Permission;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;


@Entity
@Table(name = "modules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"permissions"})
public class Module {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20)
    private String name;

    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Permission> permissions;
}
