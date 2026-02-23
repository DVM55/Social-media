package org.example.socialmediaapp.service;

import org.example.socialmediaapp.dto.res.AccountResponse;
import org.example.socialmediaapp.dto.res.PagingResponse;

public interface FollowService {
    boolean toggleFollow(Long targetAccountId);

    PagingResponse<AccountResponse> queryFollowersOfUser(
            Long targetAccountId,
            Long lastId,
            int limit,
            String keyword
    );

    PagingResponse<AccountResponse> queryFollowingsOfUser(
            Long targetAccountId,
            Long lastId,
            int limit,
            String keyword
    );
}
