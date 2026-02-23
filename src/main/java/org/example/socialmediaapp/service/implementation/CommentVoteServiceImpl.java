package org.example.socialmediaapp.service.implementation;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.socialmediaapp.dto.res.AccountResponse;
import org.example.socialmediaapp.dto.res.PagingResponse;
import org.example.socialmediaapp.entity.Account;
import org.example.socialmediaapp.entity.Comment;
import org.example.socialmediaapp.entity.CommentVote;
import org.example.socialmediaapp.entity.CommentVoteId;
import org.example.socialmediaapp.integration.minio.MinioChannel;
import org.example.socialmediaapp.repository.AccountRepository;
import org.example.socialmediaapp.repository.CommentRepository;
import org.example.socialmediaapp.repository.CommentVoteRepository;
import org.example.socialmediaapp.service.CommentVoteService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CommentVoteServiceImpl implements CommentVoteService {
    private final CommentVoteRepository commentVoteRepository;
    private final AccountRepository accountRepository;
    private final CommentRepository commentRepository;
    private final MinioChannel minioChannel;

    @Override
    public boolean toggleVoteComment(Long targetCommentId){
        Account user = getCurrentAccount();
        Comment comment = commentRepository.findById(targetCommentId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy bình luận với id: " + targetCommentId));

        boolean exists = commentVoteRepository.existsByCommentAndUser(comment, user);
        if(exists){
            commentVoteRepository.deleteByCommentIdAndUserId(comment.getId(), user.getId());
            comment.decrementVoteCount();
            commentRepository.save(comment);
            return false;
        }else {
            CommentVote commentVote = CommentVote.builder()
                    .id(new CommentVoteId(comment.getId(), user.getId()))
                    .comment(comment)
                    .user(user)
                    .build();
            commentVoteRepository.save(commentVote);
            comment.incrementVoteCount();
            commentRepository.save(comment);
            return  true;
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
        List<Map<String, Object>> rows = commentVoteRepository.queryUserCommentVote(
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
                .message("Lấy danh sách người vote bình luận thành công")
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
