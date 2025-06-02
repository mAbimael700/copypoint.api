package com.copypoint.api.domain.store;

import com.copypoint.api.domain.copypoint.Copypoint;
import com.copypoint.api.domain.role.StoreUserRole;
import com.copypoint.api.domain.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "stores")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Store {

    @Id

    private Long id;

    @Column(length = 20)
    private String name;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Copypoint> copypoints;

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<StoreUserRole> storeUserRoles;

}
