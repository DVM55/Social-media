package org.example.socialmediaapp.service;

import org.example.socialmediaapp.dto.req.CreatePostRequest;
import org.example.socialmediaapp.dto.res.PostResponse;

import java.util.List;

public interface PostService {
    void createPost(CreatePostRequest request);

    void deletePost(Long id);

    List<PostResponse> getAllPosts();

    List<PostResponse> getAllPostsByUser();

    void sharePost(CreatePostRequest request, Long originalPostId);
}
