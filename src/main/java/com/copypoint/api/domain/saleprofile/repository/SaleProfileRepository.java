package com.copypoint.api.domain.saleprofile.repository;

import com.copypoint.api.domain.saleprofile.SaleProfile;
import com.copypoint.api.domain.saleprofile.SaleProfileId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleProfileRepository extends JpaRepository<SaleProfile, SaleProfileId> {
}
