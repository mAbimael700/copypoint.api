package com.copypoint.api.domain.sale.repository;

import com.copypoint.api.domain.sale.Sale;
import com.copypoint.api.domain.sale.SaleStatus;
import org.springframework.beans.PropertyValues;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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

    @Query(value = """
        SELECT DATE(s.created_at) as sale_date,
               COALESCE(SUM(s.total_sale), 0) as total_sales,
               COUNT(s.id) as transaction_count
        FROM sales s
        WHERE s.created_at BETWEEN :startDate AND :endDate
        GROUP BY DATE(s.created_at)
        ORDER BY sale_date
        """, nativeQuery = true)
    List<Object[]> findSalesTimelineData(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    @Query(value = """
        SELECT c.id as copypoint_id,
               c.name as copypoint_name,
               COALESCE(SUM(s.total_sale), 0) as total_sales,
               COUNT(s.id) as transaction_count
        FROM copypoints c
        LEFT JOIN sales s ON c.id = s.copypoint_id
            AND s.created_at BETWEEN :startDate AND :endDate
        GROUP BY c.id, c.name
        ORDER BY total_sales DESC
        """, nativeQuery = true)
    List<Object[]> findSalesByCopypoint(@Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    @Query(value = """
        SELECT c.id as copypoint_id,
               c.name as copypoint_name,
               COALESCE(SUM(s.total_sale), 0) as total_sales,
               COUNT(s.id) as transaction_count,
               CASE WHEN COUNT(s.id) > 0
                    THEN COALESCE(SUM(s.total_sale), 0) / COUNT(s.id)
                    ELSE 0 END as average_per_transaction
        FROM copypoints c
        LEFT JOIN sales s ON c.id = s.copypoint_id
            AND s.created_at BETWEEN :startDate AND :endDate
        GROUP BY c.id, c.name
        ORDER BY total_sales DESC
        """, nativeQuery = true)
    List<Object[]> findCopypointPerformance(@Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);

    @Query(value = """
        SELECT DATE(s.created_at) as sale_date,
               c.id as copypoint_id,
               c.name as copypoint_name,
               COALESCE(SUM(s.total_sale), 0) as sales,
               COUNT(s.id) as transactions
        FROM copypoints c
        LEFT JOIN sales s ON c.id = s.copypoint_id
            AND s.created_at BETWEEN :startDate AND :endDate
        GROUP BY DATE(s.created_at), c.id, c.name
        ORDER BY sale_date, c.name
        """, nativeQuery = true)
    List<Object[]> findCopypointTrends(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

}
