package org.example.socialmediaapp.service.implementation;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.socialmediaapp.dto.res.AccountResponse;
import org.example.socialmediaapp.dto.res.PagingResponse;
import org.example.socialmediaapp.entity.Account;
import org.example.socialmediaapp.entity.Reply;
import org.example.socialmediaapp.entity.ReplyVote;
import org.example.socialmediaapp.integration.minio.MinioChannel;
import org.example.socialmediaapp.repository.AccountRepository;
import org.example.socialmediaapp.repository.ReplyRepository;
import org.example.socialmediaapp.repository.ReplyVoteRepository;
import org.example.socialmediaapp.service.ReplyVoteService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReplyVoteServiceImpl implements ReplyVoteService {
    private final ReplyVoteRepository replyVoteRepository;
    private final AccountRepository accountRepository;
    private final ReplyRepository replyRepository;
    private final MinioChannel minioChannel;

    @Override
    public boolean toggleVoteReply(Long targetReplyId){
        Account user = getCurrentAccount();
        Reply reply = replyRepository.findById(targetReplyId)
                .orElseThrow(()-> new EntityNotFoundException("Không tìm thấy phản hồi với id:" + targetReplyId));

        boolean exists = replyVoteRepository.existsByReplyAndUser(reply,user);
        if(exists){
            replyVoteRepository.deleteByReplyAndUser(reply,user);
            reply.decrementVoteCount();
            replyRepository.save(reply);
            return false;
        }else{
            ReplyVote replyVote = ReplyVote.builder()
                    .reply(reply)
                    .user(user)
                    .build();
            replyVoteRepository.save(replyVote);
            reply.incrementVoteCount();
            replyRepository.save(reply);
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
        List<Map<String, Object>> rows = replyVoteRepository.queryUserReplyVote(
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
                .message("Lấy danh sách người vote phản hồi thành công")
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
