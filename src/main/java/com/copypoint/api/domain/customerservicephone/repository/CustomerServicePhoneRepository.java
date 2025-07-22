package com.copypoint.api.domain.customerservicephone.repository;

import com.copypoint.api.domain.customerservicephone.CustomerServicePhone;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerServicePhoneRepository extends JpaRepository<CustomerServicePhone, Long> {
    Optional<CustomerServicePhone> findByPhoneNumber(String phoneNumber);

    Page<CustomerServicePhone> findByCopypointId(Long copypointId, Pageable pageable);

    // Y en CustomerServicePhoneRepository.java agregar:
    @Query("SELECT csp FROM CustomerServicePhone csp JOIN FETCH csp.messagingConfig WHERE csp.id = :id")
    Optional<CustomerServicePhone> findByIdWithMessagingConfig(@Param("id") Long id);
}
