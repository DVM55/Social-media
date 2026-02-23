package org.example.socialmediaapp.repository;

import org.example.socialmediaapp.entity.Account;
import org.example.socialmediaapp.entity.Reply;
import org.example.socialmediaapp.entity.ReplyVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface ReplyVoteRepository extends JpaRepository<ReplyVote, Long> {
    boolean existsByReplyAndUser(Reply reply, Account user);
    void deleteByReplyAndUser(Reply reply, Account user);

    @Query("""
        SELECT rv.reply.id
        FROM ReplyVote rv
        WHERE rv.user.id = :userId
            AND rv.reply.comment.id = :commentId
    """)
    List<Long> findVotedReplyIds(
            @Param("userId") Long userId,
            @Param("commentId") Long commentId
    );

    @Query(value = """
    SELECT 
        rv.user_id AS id,
        a.username AS username,
        a.object_key AS object_key,
        CASE 
            WHEN f.follower_id IS NOT NULL THEN TRUE 
            ELSE FALSE 
        END AS is_followed_by_viewer
    FROM reply_votes rv
    JOIN accounts a ON rv.user_id = a.id
    LEFT JOIN follows f 
        ON f.follower_id = :viewerId 
        AND f.following_id = rv.user_id
    WHERE rv.reply_id = :targetId
        AND (:lastId IS NULL OR rv.user_id > :lastId)
        AND unaccent(lower(a.username)) LIKE unaccent(lower(CONCAT('%', :keyword, '%')))
    LIMIT :limit
    """, nativeQuery = true)
    List<Map<String, Object>> queryUserReplyVote(
            @Param("viewerId") Long viewerId,
            @Param("targetId") Long replyId,
            @Param("keyword") String keyword,
            @Param("lastId") Long lastId,
            @Param("limit") int limit
    );

}
