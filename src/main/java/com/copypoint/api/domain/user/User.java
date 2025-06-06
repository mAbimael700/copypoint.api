package com.copypoint.api.domain.user;

import com.copypoint.api.domain.administrator.Administrator;
import com.copypoint.api.domain.client.Client;
import com.copypoint.api.domain.copypoint.Copypoint;
import com.copypoint.api.domain.person.Person;
import com.copypoint.api.domain.employees.Employee;
import com.copypoint.api.domain.sale.Sale;
import com.copypoint.api.domain.store.Store;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
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

    @Column(length = 50)
    private String email;

    private String username;

    private String password;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", referencedColumnName = "id")
    private Person personalInformation;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Store> ownedStores;

    @OneToMany(mappedBy = "responsible", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Copypoint> responsibleCopypoints;

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Copypoint> createdCopypoints;

    // Relación en qué sucursales copypoint es empleado este usuario
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Employee> employees;

    // Relación con las ventas realizadas por este usuario
    @OneToMany(mappedBy = "userVendor", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<Sale> sales = new ArrayList<>();

    // Relación: en qué tiendas es cliente este usuario
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<Client> clientRelationships = new ArrayList<>();

    // Relación en qué tiendas es administrador este usuario
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<Administrator> administrators = new ArrayList<>();
}