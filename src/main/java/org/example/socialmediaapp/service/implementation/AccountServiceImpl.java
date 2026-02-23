package org.example.socialmediaapp.service.implementation;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.socialmediaapp.dto.req.ChangePasswordRequest;
import org.example.socialmediaapp.dto.req.UpdateAccountRequest;
import org.example.socialmediaapp.dto.req.UpdateAvatarRequest;
import org.example.socialmediaapp.dto.res.*;
import org.example.socialmediaapp.entity.Account;
import org.example.socialmediaapp.entity.UserDetail;
import org.example.socialmediaapp.integration.minio.MinioChannel;
import org.example.socialmediaapp.mapper.AccountMapper;
import org.example.socialmediaapp.repository.*;
import org.example.socialmediaapp.service.AccountService;
import org.example.socialmediaapp.service.RedisService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final UserDetailRepository userDetailRepository;
    private final PasswordEncoder passwordEncoder;
    private final MinioChannel minioChannel;
    private final FollowRepository followRepository;
    private final PostRepository postRepository;

    @Override
    public List<Account> getAccounts(int page, int size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("id").ascending()
        );

        Page<Account> accountPage = accountRepository.findAll(pageable);

        return accountPage.getContent();
    }

    @Override
    public ProfilePersonalResponse getProfilePersonal(){
        Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Map<String,Object> map = accountRepository.findAccountDetail(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tài khoản với id: " + currentUserId));
        return ProfilePersonalResponse.builder()
                .id((Long) map.get("id"))
                .username((String) map.get("username"))
                .email((String) map.get("email"))
                .avatarUrl(minioChannel.getPresignedUrlSafe((String) map.get("object_key"), 86400))
                .phone((String) map.get("phone"))
                .date_of_birth((LocalDate) map.get("date_of_birth"))
                .address((String) map.get("address"))
                .gender((String) map.get("gender"))
                .count_folowing(followRepository.countFollowings(currentUserId))
                .count_follower(followRepository.countFollowers(currentUserId))
                .count_post(postRepository.countPostsByUser(currentUserId))
                .build();
    }

    @Override
    public ProfileUserResponse getProfileUser(Long userId){
        Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Map<String,Object> map = accountRepository.findAccountDetail(userId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tài khoản với id: " + userId));
        return ProfileUserResponse.builder()
                .id((Long) map.get("id"))
                .username((String) map.get("username"))
                .email((String) map.get("email"))
                .avatarUrl(minioChannel.getPresignedUrlSafe((String) map.get("object_key"), 86400))
                .phone((String) map.get("phone"))
                .date_of_birth((LocalDate) map.get("date_of_birth"))
                .address((String) map.get("address"))
                .gender((String) map.get("gender"))
                .following(followRepository.existsByFollowerIdAndFollowingId(userId, currentUserId))
                .count_folowing(followRepository.countFollowings(currentUserId))
                .count_follower(followRepository.countFollowers(currentUserId))
                .count_post(postRepository.countPostsByUser(currentUserId))
                .build();
    }

    @Override
    public void deleteAccountById(Long id) {
        accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tài khoản với id: " + id));
        accountRepository.deleteById(id);
    }

    @Override
    public boolean updateLockStatus(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tài khoản với id: " + accountId));
        boolean newLockedState = !account.isLocked(); // đảo trạng thái

        account.setLocked(newLockedState);
        accountRepository.save(account);

        return newLockedState;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UpdateAccountResponse updateAccount(UpdateAccountRequest req) {
        Account currentAccount = getCurrentAccount();

        if (!currentAccount.getUsername().equals(req.getUsername())
                && accountRepository.existsByUsername(req.getUsername())) {
            throw new IllegalArgumentException("Tên người dùng đã tồn tại");
        }

        if (!currentAccount.getEmail().equals(req.getEmail())
                && accountRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email đã được sử dụng");
        }

        // cập nhật dữ liệu account
        accountMapper.updateAccountFromDTO(req, currentAccount);

        // lấy hoặc tạo mới UserDetail
        UserDetail userDetail = userDetailRepository
                .findByAccountId(currentAccount.getId())
                .orElseGet(() -> {
                    UserDetail ud = new UserDetail();
                    ud.setAccount(currentAccount);
                    return ud;
                });

        // cập nhật dữ liệu userDetail
        accountMapper.updateUserDetailFromDTO(req, userDetail);

        // save
        userDetailRepository.save(userDetail);
        accountRepository.save(currentAccount);

        return UpdateAccountResponse.builder()
                .id(currentAccount.getId())
                .username(currentAccount.getUsername())
                .email(currentAccount.getEmail())
                .phone(userDetail.getPhone())
                .address(userDetail.getAddress())
                .gender(userDetail.getGender().name())
                .date_of_birth(userDetail.getDate_of_birth())
                .build();
    }

    @Override
    public void changePassword(ChangePasswordRequest req) {
        Account currentAccount = getCurrentAccount();
        if (!passwordEncoder.matches(req.getOldPassword(), currentAccount.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu cũ không chính xác");
        }

        currentAccount.setPassword(passwordEncoder.encode(req.getNewPassword()));
        accountRepository.save(currentAccount);
    }

    @Override
    public Map<String, String> updateAvatarUrl(UpdateAvatarRequest request) {
        Account currentAccount = getCurrentAccount();

        MultipartFile file = request.getFile();

        try (InputStream is = file.getInputStream()) {

            // Upload file lên MinIO
            Map<String, String> result = minioChannel.uploadFile(
                    file.getOriginalFilename(),
                    is,
                    file.getSize(),
                    file.getContentType(),
                    3600
            );
            String objectKey = result.get("objectKey");

            // Cập nhật DB
            currentAccount.setObject_key(objectKey);
            accountRepository.save(currentAccount);

            return Map.of("avatarUrl", result.get("url"));

        } catch (IOException e) {
            throw new RuntimeException("Không thể đọc dữ liệu từ file upload", e);

        } catch (Exception e) {
            throw new RuntimeException("Upload avatar thất bại", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PagingResponse<AccountResponse> queryAccountWithFollowStatus(
            Long lastId,
            int limit,
            String keyword
    ) {
        Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Map<String, Object>> rows = accountRepository.queryAccountWithFollowStatus(keyword, lastId, limit, currentUserId);

        List<AccountResponse> data = rows.stream()
                .map(row -> AccountResponse.builder()
                        .id(((Number) row.get("id")).longValue())
                        .username((String) row.get("username"))
                        .avatarUrl(minioChannel.getPresignedUrlSafe((String) row.get("object_key"), 86400))
                        .following((Boolean) row.get("isFollowing"))
                        .build())
                .toList();

        long total = data.size();

        return PagingResponse.<AccountResponse>builder()
                .code(200)
                .message("Lấy danh sách tài khoản thành công")
                .totalElements(total)
                .data(data)
                .build();
    }



    private Account getCurrentAccount() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return accountRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tài khoản với id: " + userId));
    }


}
