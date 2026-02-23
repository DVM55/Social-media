package org.example.socialmediaapp.service;

import org.example.socialmediaapp.dto.req.CreateCommentRequest;
import org.example.socialmediaapp.dto.res.ReplyResponse;

import java.util.List;

public interface ReplyService {
    ReplyResponse createReplyComment(CreateCommentRequest createCommentRequest, Long commentId, Long replyParentId);

    void deleteReplyById(Long replyId);

    List<ReplyResponse> getAllRepliesByCommentId(Long commentId);
}
