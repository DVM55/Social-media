package org.example.socialmediaapp.repository;

import org.example.socialmediaapp.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    @Query("""
        SELECT c FROM Conversation c
        WHERE SIZE(c.memberStatus) = 2
        AND EXISTS (
            SELECT 1 FROM c.memberStatus m WHERE m.account.id = :userId1
        )
        AND EXISTS (
            SELECT 1 FROM c.memberStatus m WHERE m.account.id = :userId2
        )
    """)
    Optional<Conversation> findDirectConversationBetween(Long userId1, Long userId2);

    @Query("""
        SELECT c
        FROM Conversation c
        JOIN c.memberStatus ms
        WHERE ms.account.id = :accountId
            AND ms.lastMessageId IS NOT NULL
    """)
    List<Conversation> findAllByAccount(Long accountId);

    @Query(value = """
        SELECT 
            c.id AS conversationId,
            cms.last_message_id AS lastMessageId,
            cms.delete_until_message_id AS deleteUntilMessageId
        FROM conversations c
        JOIN conversation_member_status cms
            ON cms.conversation_id = c.id AND cms.account_id = :userId
        WHERE EXISTS (
            SELECT 1 
            FROM conversation_member_status cms2
            WHERE cms2.conversation_id = c.id 
                AND cms2.account_id = :receiverId
        )
    """, nativeQuery = true)
    Optional<Map<String, Object>> findDirectConversationWithLastMessageAndDeleteUntil(
            Long userId, Long receiverId
    );



}
