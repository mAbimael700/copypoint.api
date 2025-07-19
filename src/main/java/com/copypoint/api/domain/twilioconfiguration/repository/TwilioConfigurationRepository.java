package com.copypoint.api.domain.twilioconfiguration.repository;

import com.copypoint.api.domain.twilioconfiguration.TwilioConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TwilioConfigurationRepository extends JpaRepository<TwilioConfiguration, Long> {
    /**
     * Encuentra todas las configuraciones activas
     */
    List<TwilioConfiguration> findByIsActiveTrue();
}
