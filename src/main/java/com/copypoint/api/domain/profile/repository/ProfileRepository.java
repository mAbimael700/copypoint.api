package com.copypoint.api.domain.profile.repository;

import com.copypoint.api.domain.profile.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

    Page<Profile> findByServicesId(Long serviceId, Pageable pageable);
}
