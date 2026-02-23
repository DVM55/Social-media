package org.example.socialmediaapp.repository;

import org.example.socialmediaapp.entity.Account;
import org.example.socialmediaapp.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    boolean existsByFollowerAndFollowing(Account follower, Account following);

    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

    void deleteByFollowerAndFollowing(Account follower, Account following);

    //Lấy danh sách followers của target, và xem viewer có follow họ không
    @Query(value = """
    SELECT 
        f.follower_id AS id,
        a.username AS username,
        a.object_key AS object_key,
        CASE WHEN vf.follower_id IS NOT NULL THEN TRUE ELSE FALSE END AS is_followed_by_viewer
    FROM follows f
    JOIN accounts a ON f.follower_id = a.id
    LEFT JOIN follows vf 
        ON vf.follower_id = :viewerId 
        AND vf.following_id = f.follower_id
    WHERE f.following_id = :targetId
        AND (:lastId IS NULL OR f.follower_id > :lastId)
        AND unaccent(lower(a.username)) LIKE unaccent(lower(CONCAT('%', :keyword, '%')))
    LIMIT :limit
    """, nativeQuery = true)
    List<Map<String, Object>> queryFollowersOfUser(
            @Param("viewerId") Long viewerId,
            @Param("targetId") Long targetId,
            @Param("keyword") String keyword,
            @Param("lastId") Long lastId,
            @Param("limit") int limit
    );

    //Lấy danh sách followings của target, và xem viewer có follow họ không
    @Query(value = """
    SELECT 
        f.following_id AS id,
        a.username AS username,
        a.object_key AS object_key,
        CASE WHEN vf.follower_id IS NOT NULL THEN TRUE ELSE FALSE END AS is_followed_by_viewer
    FROM follows f
    JOIN accounts a ON f.following_id = a.id
    LEFT JOIN follows vf 
        ON vf.follower_id = :viewerId 
        AND vf.following_id = f.following_id
    WHERE f.follower_id = :targetId
        AND (:lastId IS NULL OR f.following_id > :lastId)
        AND unaccent(lower(a.username)) LIKE unaccent(lower(CONCAT('%', :keyword, '%')))
    LIMIT :limit
    """, nativeQuery = true)
    List<Map<String, Object>> queryFollowingsOfUser(
            @Param("viewerId") Long viewerId,
            @Param("targetId") Long targetId,
            @Param("keyword") String keyword,
            @Param("lastId") Long lastId,
            @Param("limit") int limit
    );

    @Query("SELECT f.following.id FROM Follow f WHERE f.follower.id = :userId")
    List<Long> findAllFollowingIdsByUser(Long userId);

    @Query(value = """
        SELECT COUNT(*) 
            FROM follows 
            WHERE following_id = :userId
        """, nativeQuery = true)
    Long countFollowers(@Param("userId") Long userId);

    @Query(value = """
        SELECT COUNT(*) 
            FROM follows 
            WHERE follower_id = :userId
        """, nativeQuery = true)
    Long countFollowings(@Param("userId") Long userId);

}
