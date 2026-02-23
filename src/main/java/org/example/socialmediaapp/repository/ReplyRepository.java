package org.example.socialmediaapp.repository;

import org.example.socialmediaapp.entity.Reply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReplyRepository extends JpaRepository<Reply, Long> {
    @Query("""
        SELECT rp
        FROM Reply rp
        JOIN FETCH rp.user u
        WHERE rp.comment.id = :commentId
    """)
    List<Reply> findRepliesWithUser(@Param("commentId") Long commentId);

}
