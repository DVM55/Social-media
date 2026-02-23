package org.example.socialmediaapp.service.implementation;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.socialmediaapp.dto.res.AccountResponse;
import org.example.socialmediaapp.dto.res.PagingResponse;
import org.example.socialmediaapp.entity.Account;

import org.example.socialmediaapp.entity.Post;
import org.example.socialmediaapp.entity.PostVote;
import org.example.socialmediaapp.entity.PostVoteId;
import org.example.socialmediaapp.integration.minio.MinioChannel;
import org.example.socialmediaapp.repository.AccountRepository;
import org.example.socialmediaapp.repository.PostRepository;
import org.example.socialmediaapp.repository.PostVoteRepository;
import org.example.socialmediaapp.service.PostVoteService;
import org.example.socialmediaapp.service.RedisService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PostVoteServiceImpl implements PostVoteService {
    private final PostVoteRepository postVoteRepository;
    private final AccountRepository accountRepository;
    private final PostRepository postRepository;
    private final MinioChannel minioChannel;

    @Override
    @Transactional
    public boolean toggleVotePost(Long targetPostId){
        Account user = getCurrentAccount();
        Post post = postRepository.findById(targetPostId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy bài viết với id: " + targetPostId));
        boolean exists = postVoteRepository.existsByPostAndUser(post, user);
        if(exists){
            postVoteRepository.deleteByPostIdAndUserId(post.getId(), user.getId());
            post.decrementVoteCount();
            postRepository.save(post);
            return false;
        }else{
            PostVote vote = new PostVote(
                    new PostVoteId(targetPostId, user.getId()),
                    post,
                    user
            );
            postVoteRepository.save(vote);
            post.incrementVoteCount();
            postRepository.save(post);
            return true;
        }
    }

    @Override
    public PagingResponse<AccountResponse> queryUserVote(
            Long postId,
            Long lastId,
            int limit,
            String keyword
    ){
        Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Map<String, Object>> rows = postVoteRepository.queryUserVote(
                currentUserId,
                postId,
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
                .message("Lấy danh sách người vote bài viết thành công")
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
