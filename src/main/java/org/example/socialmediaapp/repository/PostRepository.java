package org.example.socialmediaapp.repository;

import org.example.socialmediaapp.entity.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post,Long> {
    @Query("SELECT p FROM Post p ORDER BY p.createdAt DESC")
    List<Post> findAllPosts();

    @Query("""
        SELECT p FROM Post p
        WHERE p.user.id = :userId
        ORDER BY p.createdAt DESC
    """)
    List<Post> findAllPostsByUser(@Param("userId") Long userId);


    @Query("""
        SELECT p FROM Post p
        WHERE p.id IN :ids
    """)
    List<Post> findOriginalPosts(@Param("ids") List<Long> ids);

    @Query("""
    SELECT COUNT(p)
    FROM Post p
    WHERE p.user.id = :userId
    """)
    Long countPostsByUser(@Param("userId") Long userId);

}
