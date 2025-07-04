package com.copypoint.api.domain.copypoint.repository;

import com.copypoint.api.domain.copypoint.Copypoint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CopypointRepository extends JpaRepository<Copypoint, Long> {
    Page<Copypoint> findAllByStoreId(Pageable pageable, Long storeId);
}
