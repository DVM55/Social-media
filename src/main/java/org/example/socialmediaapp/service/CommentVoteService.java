package org.example.socialmediaapp.service;

import org.example.socialmediaapp.dto.res.AccountResponse;
import org.example.socialmediaapp.dto.res.PagingResponse;

public interface CommentVoteService {
    boolean toggleVoteComment(Long targetCommentId);

    PagingResponse<AccountResponse> queryUserVote(
            Long postId,
            Long lastId,
            int limit,
            String keyword
    );
}
