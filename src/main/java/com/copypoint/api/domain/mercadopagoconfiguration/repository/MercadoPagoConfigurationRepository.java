package com.copypoint.api.domain.mercadopagoconfiguration.repository;

import com.copypoint.api.domain.mercadopagoconfiguration.MercadoPagoConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MercadoPagoConfigurationRepository extends JpaRepository<MercadoPagoConfiguration, Long> {
    @Query("SELECT mpc FROM MercadoPagoConfiguration mpc WHERE mpc.copypoint.id = :copypointId AND mpc.isActive = true")
    Optional<MercadoPagoConfiguration> findActiveByCopypointId(@Param("copypointId") Long copypointId);
}
