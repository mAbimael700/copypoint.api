package com.copypoint.api.domain.whatsappbussinessconfiguration.repository;

import com.copypoint.api.domain.whatsappbussinessconfiguration.WhatsAppBusinessConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface WhatsAppBusinessConfigurationRepository extends JpaRepository<WhatsAppBusinessConfiguration, Long> {
    @Query("SELECT w FROM WhatsAppBusinessConfiguration w WHERE w.phoneNumberId = :phoneNumberId")
    Optional<WhatsAppBusinessConfiguration> findByPhoneNumberId(@Param("phoneNumberId") String phoneNumberId);

    @Query("SELECT w FROM WhatsAppBusinessConfiguration w WHERE w.businessAccountId = :businessAccountId AND w.isActive = true")
    Optional<WhatsAppBusinessConfiguration> findActiveByBusinessAccountId(@Param("businessAccountId") String businessAccountId);
}
