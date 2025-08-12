package com.copypoint.api.domain.saleprofile.repository;

import com.copypoint.api.domain.saleprofile.SaleProfile;
import com.copypoint.api.domain.saleprofile.SaleProfileId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SaleProfileRepository extends JpaRepository<SaleProfile, SaleProfileId> {

    List<SaleProfile> findByIdSaleId(Long saleId);
    Page<SaleProfile> findByIdSaleId(Long saleId, Pageable pageable);

    @Query(value = """
        SELECT srv.id as service_id,
               srv.name as service_name,
               SUM(sp.quantity) as quantity_sold,
               SUM(sp.subtotal) as total_revenue
        FROM sale_profiles sp
        INNER JOIN services srv ON sp.service_id = srv.id
        INNER JOIN sales s ON sp.sale_id = s.id
        WHERE s.created_at BETWEEN :startDate AND :endDate
        GROUP BY srv.id, srv.name
        ORDER BY quantity_sold DESC
        LIMIT :limitResults
        """, nativeQuery = true)
    List<Object[]> findTopServices(@Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate,
                                   @Param("limitResults") Integer limitResults);

    @Query(value = """
        SELECT DATE(s.created_at) as sale_date,
               srv.name as service_name,
               SUM(sp.quantity) as quantity_sold,
               SUM(sp.subtotal) as revenue
        FROM sale_profiles sp
        INNER JOIN services srv ON sp.service_id = srv.id
        INNER JOIN sales s ON sp.sale_id = s.id
        WHERE s.created_at BETWEEN :startDate AND :endDate
        AND srv.id IN :serviceIds
        GROUP BY DATE(s.created_at), srv.id, srv.name
        ORDER BY sale_date, srv.name
        """, nativeQuery = true)
    List<Object[]> findServiceTrends(@Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate,
                                     @Param("serviceIds") List<Long> serviceIds);
}
