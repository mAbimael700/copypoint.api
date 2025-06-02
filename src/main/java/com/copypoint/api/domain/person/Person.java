package com.copypoint.api.domain.person;

import com.copypoint.api.domain.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name="persons")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "first_name", length = 20)
    private String firstName;
    @Column(name = "last_name", length = 20)
    private String lastName;
    @Column(length = 50)
    private String email;
    @Column(name = "phone_number", length = 15)
    private String phoneNumber;
    // Relación bidireccional con User
    @OneToMany(mappedBy = "personalInformation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<User> users;
}
