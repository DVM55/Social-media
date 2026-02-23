package org.example.socialmediaapp.repository;

import org.example.socialmediaapp.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query(value = """
        SELECT m.*
        FROM messages m
        WHERE m.conversation_id = :conversationId
          AND (:deleteUntilMessageId IS NULL OR m.id > :deleteUntilMessageId)
          AND (:lastMessageId IS NULL OR m.id <= :lastMessageId)
        ORDER BY m.id DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Message> findLatestMessages(
            @Param("conversationId") Long conversationId,
            @Param("deleteUntilMessageId") Long deleteUntilMessageId,
            @Param("lastMessageId") Long lastMessageId,
            @Param("limit") int limit
    );

    // Kiểm tra còn tin nhắn cũ hơn không
    boolean existsByConversationIdAndIdLessThan(Long conversationId, Long messageId);
}
