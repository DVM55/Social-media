package org.example.socialmediaapp.repository;

import org.example.socialmediaapp.entity.Account;
import org.example.socialmediaapp.entity.Comment;
import org.example.socialmediaapp.entity.CommentVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface CommentVoteRepository extends JpaRepository<CommentVote, Long> {
    boolean existsByCommentAndUser(Comment comment, Account user);

    @Modifying
    @Query("""
        delete from CommentVote cv
        where cv.comment.id = :commentId and cv.user.id = :userId
    """)
    void deleteByCommentIdAndUserId(Long commentId, Long userId);

    @Query("""
        SELECT cv.id.commentId
        FROM CommentVote cv
        WHERE cv.user.id = :userId
            AND cv.comment.post.id = :postId
    """)
    List<Long> findVotedCommentIds(
            @Param("userId") Long userId,
            @Param("postId") Long postId);

    @Query(value = """
    SELECT 
        cv.user_id AS id,
        a.username AS username,
        a.object_key AS object_key,
        CASE 
            WHEN f.follower_id IS NOT NULL THEN TRUE 
            ELSE FALSE 
        END AS is_followed_by_viewer
    FROM comment_votes cv
    JOIN accounts a ON cv.user_id = a.id
    LEFT JOIN follows f 
        ON f.follower_id = :viewerId 
        AND f.following_id = cv.user_id
    WHERE cv.comment_id = :targetId
        AND (:lastId IS NULL OR cv.user_id > :lastId)
        AND unaccent(lower(a.username)) LIKE unaccent(lower(CONCAT('%', :keyword, '%')))
    LIMIT :limit
    """, nativeQuery = true)
    List<Map<String, Object>> queryUserCommentVote(
            @Param("viewerId") Long viewerId,
            @Param("targetId") Long targetId,
            @Param("keyword") String keyword,
            @Param("lastId") Long lastId,
            @Param("limit") int limit
    );


}
