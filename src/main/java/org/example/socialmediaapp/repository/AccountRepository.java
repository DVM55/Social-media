package org.example.socialmediaapp.repository;

import org.example.socialmediaapp.entity.Account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByEmail(String email);

    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    @Query("""
        SELECT a.object_key
        FROM Account a
        WHERE a.id = :userId
    """)
    String findObjectKeyByUserId(@Param("userId") Long userId);

    @Query(
            value = """
        SELECT *
        FROM accounts
        ORDER BY id ASC
        LIMIT 30 OFFSET 50000
    """,
            nativeQuery = true
    )
    List<Account> findTop30Native();

    List<Account> findTop20ByOrderByIdAsc();

    @Query(
            value = """
        select *
        from accounts
        where id > :lastId
        limit 20
    """,
            nativeQuery = true
    )
    List<Account> findTop20ByIdGreaterThanOrderByIdAsc(
            @Param("lastId") Long lastId
    );


    @Query(
            value = """
        SELECT *
        FROM accounts
        ORDER BY id ASC
        LIMIT 50
    """,
            nativeQuery = true
    )
    List<Account> findTop50Native();

    @Query(
            value = """
        SELECT id, username
        FROM accounts
        ORDER BY id ASC
        LIMIT 50
    """,
            nativeQuery = true
    )
    List<Object[]> findTop50BasicNative();














    @Query(value = """
        SELECT 
            a.id AS id,
            a.username AS username,
            a.object_key AS object_key,
            CASE WHEN f.follower_id IS NOT NULL THEN TRUE ELSE FALSE END AS is_following
        FROM accounts a
        LEFT JOIN follows f 
            ON f.following_id = a.id 
            AND f.follower_id = :currentUserId
        WHERE (:lastId IS NULL OR a.id > :lastId)
            AND a.locked = false
            AND a.role = 'USER'
            AND a.id <> :currentUserId            
            AND unaccent(lower(a.username)) LIKE unaccent(lower(CONCAT('%', :keyword, '%')))
        LIMIT :limit
        """, nativeQuery = true
    )
    List<Map<String, Object>> queryAccountWithFollowStatus(
            @Param("keyword") String keyword,
            @Param("lastId") Long lastId,
            @Param("limit") int limit,
            @Param("currentUserId") Long currentUserId
    );

    @Query("""
        SELECT a FROM Account a
        WHERE a.id IN :ids
    """)
    List<Account> findUsersByIds(@Param("ids") List<Long> ids);

    @Query(value = """
    SELECT 
        a.id AS id,
        a.email AS email,
        a.username AS username,
        a.object_key AS objectKey,
        ud.phone AS phone,
        ud.date_of_birth AS date_of_birth,
        ud.address AS address,
        ud.gender AS gender 
    FROM accounts a
    LEFT JOIN user_details ud 
        ON ud.user_id = a.id
    WHERE a.id = :accountId
    """, nativeQuery = true)
    Optional<Map<String, Object>> findAccountDetail(@Param("accountId") Long id);

    @Query("""
        select a.object_key
        from Account a
        where a.object_key is not null
          and a.object_key <> ''
    """)
    Set<String> findAllValidObjectKeys();

}
