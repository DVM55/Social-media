package org.example.socialmediaapp.repository;

import org.example.socialmediaapp.entity.Account;
import org.example.socialmediaapp.entity.Post;
import org.example.socialmediaapp.entity.PostVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface PostVoteRepository extends JpaRepository<PostVote, Long> {
    boolean existsByPostAndUser(Post post, Account user);

    @Modifying
    @Query("delete from PostVote pv where pv.post.id = :postId and pv.user.id = :userId")
    void deleteByPostIdAndUserId(Long postId, Long userId);

    @Query(value = """
    SELECT 
        pv.user_id AS id,
        a.username AS username,
        a.object_key AS object_key,
        CASE 
            WHEN f.follower_id IS NOT NULL THEN TRUE 
            ELSE FALSE 
        END AS is_followed_by_viewer
    FROM post_votes pv
    JOIN accounts a ON pv.user_id = a.id
    LEFT JOIN follows f 
        ON f.follower_id = :viewerId 
        AND f.following_id = pv.user_id
    WHERE pv.post_id = :targetId
        AND (:lastId IS NULL OR pv.user_id > :lastId)
        AND unaccent(lower(a.username)) LIKE unaccent(lower(CONCAT('%', :keyword, '%')))
    LIMIT :limit
    """, nativeQuery = true)
    List<Map<String, Object>> queryUserVote(
            @Param("viewerId") Long viewerId,
            @Param("targetId") Long targetId,
            @Param("keyword") String keyword,
            @Param("lastId") Long lastId,
            @Param("limit") int limit
    );

    @Query("SELECT pv.post.id FROM PostVote pv WHERE pv.user.id = :userId")
    List<Long> findAllVotedPostIdsByUser(Long userId);

}
