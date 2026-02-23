package org.example.socialmediaapp.repository;

import org.example.socialmediaapp.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("""
        SELECT c
        FROM Comment c
        JOIN FETCH c.user u
        WHERE c.post.id = :postId
    """)
    List<Comment> findCommentsWithUser(@Param("postId") Long postId);


}
