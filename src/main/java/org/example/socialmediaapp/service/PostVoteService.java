package org.example.socialmediaapp.service;

import org.example.socialmediaapp.dto.res.AccountResponse;
import org.example.socialmediaapp.dto.res.PagingResponse;

public interface PostVoteService {
    boolean toggleVotePost(Long targetPostId);

    PagingResponse<AccountResponse> queryUserVote(
            Long postId,
            Long lastId,
            int limit,
            String keyword
    );
}
