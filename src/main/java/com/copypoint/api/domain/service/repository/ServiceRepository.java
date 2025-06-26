package com.copypoint.api.domain.service.repository;

import com.copypoint.api.domain.service.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {

    Page<Service> findByStoreId(Long storeId, Pageable pageable);
}
