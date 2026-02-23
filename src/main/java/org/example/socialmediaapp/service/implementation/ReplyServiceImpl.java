package org.example.socialmediaapp.service.implementation;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.socialmediaapp.dto.UserDto;
import org.example.socialmediaapp.dto.req.CreateCommentRequest;
import org.example.socialmediaapp.dto.res.ReplyResponse;
import org.example.socialmediaapp.entity.Account;
import org.example.socialmediaapp.entity.Comment;
import org.example.socialmediaapp.entity.Post;
import org.example.socialmediaapp.entity.Reply;
import org.example.socialmediaapp.integration.minio.MinioChannel;
import org.example.socialmediaapp.repository.*;
import org.example.socialmediaapp.service.ReplyService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ReplyServiceImpl implements ReplyService {
    private final ReplyRepository replyRepository;
    private final MinioChannel minioChannel;
    private final AccountRepository accountRepository;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final ReplyVoteRepository replyVoteRepository;

    @Override
    public ReplyResponse createReplyComment(CreateCommentRequest req, Long commentId, Long replyParentId) {
        Account user = getCurrentAccount();

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Không tìm thấy bình luận với id: " + commentId
                ));

        Reply replyParent = null;
        if (replyParentId != null) {
            replyParent = replyRepository.findById(replyParentId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Không tìm thấy phản hồi với id: " + replyParentId
                    ));
        }

        validateReplyCommentRequest(req);

        Reply reply = new Reply();
        reply.setComment(comment);
        reply.setUser(user);
        reply.setContent(req.getContent());

        String imageUrl = null;

        MultipartFile file = req.getFile();
        if (file != null && !file.isEmpty()) {
            try (InputStream is = file.getInputStream()) {

                Map<String, String> upload = minioChannel.uploadFile(
                        file.getOriginalFilename(),
                        is,
                        file.getSize(),
                        file.getContentType(),
                        86400 // 1 ngày
                );

                String objectKey = upload.get("objectKey");
                imageUrl = upload.get("url");

                reply.setObjectKey(objectKey);

            } catch (Exception e) {
                throw new RuntimeException("Không thể upload ảnh phản hồi", e);
            }
        }

        if (replyParent != null) {
            reply.setReplyUser(replyParent.getUser());
        }

        // lưu reply
        replyRepository.save(reply);

        // tăng commentCount cho bài viết & comment gốc
        Post post = comment.getPost();
        post.incrementCommentCount();
        postRepository.save(post);

        comment.incrementCommentCount();
        commentRepository.save(comment);

        return ReplyResponse.builder()
                .id(reply.getId())
                .user(buildUserDto(user))
                .content(reply.getContent())
                .imageUrl(imageUrl)
                .voteCount(reply.getVoteCount())
                .userVoted(false)
                .replyUserId(replyParent != null ? replyParent.getUser().getId() : null)
                .replyUserName(replyParent != null ? replyParent.getUser().getUsername() : null)
                .createdAt(reply.getCreatedAt())
                .build();
    }


    @Override
    public void deleteReplyById(Long replyId){
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Reply reply = replyRepository.findById(replyId)
                .orElseThrow(()-> new EntityNotFoundException("Không tìm thấy phản hồi với id:"+ replyId));

        if(! userId.equals(reply.getUser().getId())){
            throw new AccessDeniedException("Bạn không có quyền xóa phản hồi này");
        }
        Comment comment = reply.getComment();
        Post post = comment.getPost();

        comment.decrementCommentCount();
        post.decrementCommentCount();

        replyRepository.deleteById(replyId);
        commentRepository.save(comment);
        postRepository.save(post);
    }

    @Override
    public List<ReplyResponse> getAllRepliesByCommentId(Long commentId){

        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        List<Reply> replies = replyRepository.findRepliesWithUser(commentId);

        List<Long> replyIds = replyVoteRepository.findVotedReplyIds(userId, commentId);
        Set<Long> replyVotedSet = new HashSet<>(replyIds);

        return replies.stream()
                .map(reply -> ReplyResponse.builder()
                        .id(reply.getId())
                        .user(buildUserDto(reply.getUser()))
                        .content(reply.getContent())
                        .imageUrl(minioChannel.getPresignedUrlSafe(reply.getObjectKey(), 86400))
                        .voteCount(reply.getVoteCount())
                        .userVoted(replyVotedSet.contains(reply.getId()))
                        .replyUserId(reply.getReplyUser() != null ? reply.getReplyUser().getId() : null)
                        .replyUserName(reply.getReplyUser() != null ? reply.getReplyUser().getUsername() : null)
                        .createdAt(reply.getCreatedAt())
                        .build()
                )
                .toList();
    }


    private UserDto buildUserDto(Account user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                minioChannel.getPresignedUrlSafe(user.getObject_key(), 86400)
        );
    }

    private void validateReplyCommentRequest(CreateCommentRequest req) {
        boolean noContent = (req.getContent() == null || req.getContent().trim().isEmpty());

        boolean noFile = (req.getFile() == null || req.getFile().isEmpty());

        if (noContent && noFile) {
            throw new IllegalArgumentException("Bình luận phải có nội dung hoặc hình ảnh");
        }
    }

    private Account getCurrentAccount() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return accountRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tài khoản với id: " + userId));
    }
}
