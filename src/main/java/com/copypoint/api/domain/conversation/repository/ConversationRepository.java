package com.copypoint.api.domain.conversation.repository;

import com.copypoint.api.domain.conversation.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    // Opción 1: Usando método derivado (más simple)
    Page<Conversation> findByCustomerServicePhoneId(Long customerServicePhoneId, Pageable pageable);

    // Opción 2: Si necesitas solo la primera conversación
    Optional<Conversation> findFirstByCustomerServicePhoneId(Long customerServicePhoneId);

    // Opción 3: Ordenada por fecha de creación (más reciente primero)
    Page<Conversation> findByCustomerServicePhoneIdOrderByCreatedAtDesc(Long customerServicePhoneId, Pageable pageable);

    @Query("SELECT c FROM Conversation c " +
            "LEFT JOIN FETCH c.customerContact " +
            "LEFT JOIN FETCH c.customerServicePhone " +
            "WHERE c.customerServicePhone.id = :customerServicePhoneId")
    Page<Conversation> findConversationsWithDetailsByCustomerServicePhoneId(
            @Param("customerServicePhoneId") Long customerServicePhoneId, Pageable pageable);

    @Query("SELECT c FROM Conversation c " +
            "LEFT JOIN FETCH c.customerContact " +
            "LEFT JOIN FETCH c.customerServicePhone " +
            "WHERE c.id = :id")
    Conversation findConversationWithDetailsByConversationId(
            @Param("id") Long id);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Conversation c " +
            "WHERE c.customerContact.phoneNumber = :customerPhoneNumber " +
            "AND c.customerServicePhone.phoneNumber = :customerServicePhoneNumber")
    boolean existsByCustomerPhoneNumberAndCustomerServicePhoneNumber(
            @Param("customerPhoneNumber") String customerPhoneNumber,
            @Param("customerServicePhoneNumber") String customerServicePhoneNumber);

    long countByCustomerServicePhoneId(Long customerServicePhoneId);

}
