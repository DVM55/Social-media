package org.example.socialmediaapp.service.implementation;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.socialmediaapp.dto.res.AccountResponse;
import org.example.socialmediaapp.dto.res.PagingResponse;
import org.example.socialmediaapp.entity.Account;
import org.example.socialmediaapp.entity.Follow;
import org.example.socialmediaapp.entity.FollowId;
import org.example.socialmediaapp.integration.minio.MinioChannel;
import org.example.socialmediaapp.repository.AccountRepository;
import org.example.socialmediaapp.repository.FollowRepository;
import org.example.socialmediaapp.service.FollowService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {
    private final AccountRepository accountRepository;
    private final FollowRepository followRepository;
    private final MinioChannel minioChannel;

    @Override
    public boolean toggleFollow(Long targetAccountId) {
        Account follower = getCurrentAccount();
        Account following = accountRepository.findById(targetAccountId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tài khoản với id: " + targetAccountId));
        if (targetAccountId.equals(follower.getId())) {
            throw new IllegalArgumentException("Không thể theo dõi chính mình");
        }
        boolean exists = followRepository.existsByFollowerAndFollowing(follower, following);
        if (exists) {
            // Nếu đã follow → unfollow
            followRepository.deleteByFollowerAndFollowing(follower, following);
            return false;
        } else {
            // Nếu chưa follow → follow
            Follow entity = Follow.builder()
                    .id(new FollowId(follower.getId(), targetAccountId))
                    .follower(follower)
                    .following(following)
                    .build();
            followRepository.save(entity);
            return true;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PagingResponse<AccountResponse> queryFollowersOfUser(
            Long targetId,
            Long lastId,
            int limit,
            String keyword
    ) {
        Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        List<Map<String, Object>> rows = followRepository.queryFollowersOfUser(
                currentUserId,
                targetId,
                keyword,
                lastId,
                limit
        );

        List<AccountResponse> data = rows.stream()
                .map(row -> AccountResponse.builder()
                        .id(((Number) row.get("id")).longValue())
                        .username((String) row.get("username"))
                        .avatarUrl(minioChannel.getPresignedUrlSafe((String) row.get("object_key"), 86400))
                        .following((Boolean) row.get("is_followed_by_viewer"))
                        .build())
                .toList();

        return PagingResponse.<AccountResponse>builder()
                .code(200)
                .message("Lấy danh sách người theo dõi thành công")
                .totalElements(data.size())
                .data(data)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PagingResponse<AccountResponse> queryFollowingsOfUser(
            Long targetId,
            Long lastId,
            int limit,
            String keyword
    ) {
        Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Map<String, Object>> rows = followRepository.queryFollowingsOfUser(
                currentUserId,
                targetId,
                keyword,
                lastId,
                limit
        );

        List<AccountResponse> data = rows.stream()
                .map(row -> AccountResponse.builder()
                        .id(((Number) row.get("id")).longValue())
                        .username((String) row.get("username"))
                        .avatarUrl(minioChannel.getPresignedUrlSafe((String) row.get("object_key"), 86400))
                        .following((Boolean) row.get("is_followed_by_viewer"))
                        .build())
                .toList();
        return PagingResponse.<AccountResponse>builder()
                .code(200)
                .message("Lấy danh sách người đang theo dõi thành công")
                .totalElements(data.size())
                .data(data)
                .build();
    }

    private Account getCurrentAccount() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return accountRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tài khoản với id: " + userId));
    }
}
