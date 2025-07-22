package com.copypoint.api.domain.customerservicephone.repository;

import com.copypoint.api.domain.customerservicephone.CustomerServicePhone;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerServicePhoneRepository extends JpaRepository<CustomerServicePhone, Long> {
    Optional<CustomerServicePhone> findByPhoneNumber(String phoneNumber);

    Page<CustomerServicePhone> findByCopypointId(Long copypointId, Pageable pageable);
}
