package org.example.socialmediaapp.repository;

import org.example.socialmediaapp.entity.PostMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostMediaRepository extends JpaRepository<PostMedia,Long> {
    @Query("""
        SELECT m FROM PostMedia m
        WHERE m.post.id IN :postIds
    """)
    List<PostMedia> findAllMediasByPostIds(@Param("postIds") List<Long> postIds);
}
