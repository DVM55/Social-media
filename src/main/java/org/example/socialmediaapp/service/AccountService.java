package org.example.socialmediaapp.service;

import org.example.socialmediaapp.dto.req.ChangePasswordRequest;
import org.example.socialmediaapp.dto.req.UpdateAccountRequest;
import org.example.socialmediaapp.dto.req.UpdateAvatarRequest;
import org.example.socialmediaapp.dto.res.*;
import org.example.socialmediaapp.entity.Account;

import java.util.List;
import java.util.Map;

public interface AccountService {
    void deleteAccountById(Long id);

    boolean updateLockStatus(Long id);

    UpdateAccountResponse updateAccount(UpdateAccountRequest updateAccountRequest);
    void changePassword(ChangePasswordRequest changePasswordRequest);

    Map<String, String> updateAvatarUrl(UpdateAvatarRequest request);

    PagingResponse<AccountResponse> queryAccountWithFollowStatus(
            Long lastId,
            int limit,
            String keyword
    );

    ProfilePersonalResponse getProfilePersonal();

    ProfileUserResponse getProfileUser(Long userId);

    List<Account> getAccounts(int page, int size);


}
