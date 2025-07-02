package com.copypoint.api.domain.service.repository;

import com.copypoint.api.domain.service.Service;
import com.copypoint.api.domain.service.dto.ServiceDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {

    Page<Service> findByStoreId(Long storeId, Pageable pageable);

    /**
     * Encuentra todos los servicios que pertenecen a un copypoint específico.
     * La relación es: Service -> Store <- Copypoint (Store tiene múltiples Copypoints)
     *
     * @param copypointId el ID del copypoint
     * @param pageable configuración de paginación
     * @return página de servicios del copypoint
     */
    @Query("SELECT s FROM Service s " +
            "JOIN s.store st " +
            "JOIN st.copypoints cp " +
            "WHERE cp.id = :copypointId")
    Page<Service> findByCopypointId(@Param("copypointId") Long copypointId, Pageable pageable);

    /**
     * Encuentra todos los servicios activos que pertenecen a un copypoint específico.
     *
     * @param copypointId el ID del copypoint
     * @param pageable configuración de paginación
     * @return página de servicios activos del copypoint
     */
    @Query("SELECT s FROM Service s " +
            "JOIN s.store st " +
            "JOIN st.copypoints cp " +
            "WHERE cp.id = :copypointId " +
            "AND s.active = true")
    Page<Service> findActiveByCopypointId(@Param("copypointId") Long copypointId, Pageable pageable);

}
