package org.example.socialmediaapp.service;

import org.example.socialmediaapp.dto.req.CreateCommentRequest;
import org.example.socialmediaapp.dto.res.CommentResponse;

import java.util.List;

public interface CommentService {
    CommentResponse createComment(CreateCommentRequest createCommentRequest, Long postId);

    void deleteComment(Long id);

    List<CommentResponse> getAllComments(Long postId);
}
