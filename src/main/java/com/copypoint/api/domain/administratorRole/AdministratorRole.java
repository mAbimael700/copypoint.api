package com.copypoint.api.domain.administratorRole;

import com.copypoint.api.domain.administrator.Administrator;
import com.copypoint.api.domain.role.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "administrator_roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdministratorRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "user_id", referencedColumnName = "user_id"),
            @JoinColumn(name = "store_id", referencedColumnName = "store_id")
    })
    private Administrator administrator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", referencedColumnName = "id",
            insertable = false, updatable = false)
    private Role role;

    @Column(name = "added_at")
    private LocalDateTime addedAt;
}
