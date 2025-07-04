package com.copypoint.api.domain.saleprofile.repository;

import com.copypoint.api.domain.saleprofile.SaleProfile;
import com.copypoint.api.domain.saleprofile.SaleProfileId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SaleProfileRepository extends JpaRepository<SaleProfile, SaleProfileId> {

    List<SaleProfile> findByIdSaleId(Long saleId);
    Page<SaleProfile> findByIdSaleId(Long saleId, Pageable pageable);
}
