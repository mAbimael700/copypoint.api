package com.copypoint.api.domain.sale.repository;

import com.copypoint.api.domain.sale.Sale;
import com.copypoint.api.domain.sale.SaleStatus;
import org.springframework.beans.PropertyValues;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    /**
     * Encontrar ventas por copypoint y estado
     */
    List<Sale> findByCopypoint_IdAndStatus(Long copypointId, SaleStatus status);

    /**
     * Encontrar todas las ventas de un copypoint
     */
    List<Sale> findByCopypoint_Id(Long copypointId);

    /**
     * Encontrar todas las ventas de un copypoint
     */
    Page<Sale> findByCopypoint_Id(Long copypointId, Pageable pageable);

    /**
     * Encontrar ventas por usuario vendedor
     */
    List<Sale> findByUserVendor_Id(Long userId);

    /**
     * Encontrar ventas por estado
     */
    List<Sale> findByStatus(SaleStatus status);

    Page<Sale> findByCopypoint_IdAndStatus(Long copypointId, SaleStatus saleStatus, Pageable pageable);


    Page<Sale> findByUserVendorIdAndCopypoint_IdAndStatus(Long userId, Long copypointId, SaleStatus saleStatus, Pageable pageable);


    Page<Sale> findByUserVendorIdAndCopypoint_Id(Long userId, Long copypointId, Pageable pageable);

}
