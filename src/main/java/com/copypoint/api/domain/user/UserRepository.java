package com.copypoint.api.domain.user;

import aj.org.objectweb.asm.commons.Remapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Data JPA genera automáticamente esta consulta
    Page<User> findByStatus(UserStatus status, Pageable pageable);

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);
}
