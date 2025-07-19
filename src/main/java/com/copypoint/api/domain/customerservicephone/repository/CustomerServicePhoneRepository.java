package com.copypoint.api.domain.customerservicephone.repository;

import com.copypoint.api.domain.customerservicephone.CustomerServicePhone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerServicePhoneRepository extends JpaRepository<CustomerServicePhone, Long> {
}
