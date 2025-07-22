package com.copypoint.api.domain.message.repository;

import com.copypoint.api.domain.message.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByConversationId(Long conversationId, Pageable pageable);

    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId ORDER BY m.dateSent ASC")
    Page<Message> findByConversationIdOrderByDateSent(@Param("conversationId") Long conversationId, Pageable pageable);

    Message findByMessageSid(String messageSid);
}
