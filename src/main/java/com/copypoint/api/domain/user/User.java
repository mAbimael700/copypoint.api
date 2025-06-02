package com.copypoint.api.domain.user;

import com.copypoint.api.domain.copypoint.Copypoint;
import com.copypoint.api.domain.person.Person;
import com.copypoint.api.domain.role.UserRole;
import com.copypoint.api.domain.sales.Sale;
import com.copypoint.api.domain.store.Store;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"userRoles"})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "creation_date")
    private LocalDateTime creationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", referencedColumnName = "id")
    private Person personalInformation;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Store> ownedStores;

    @OneToMany(mappedBy = "responsible", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Copypoint> responsibleCopypoints;

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Copypoint> createdCopypoints;

    // Relación con UserRole
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserRole> userRoles;

    // Relación con las ventas realizadas por este usuario
    @OneToMany(mappedBy = "userVendor", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<Sale> sales = new ArrayList<>();
}
