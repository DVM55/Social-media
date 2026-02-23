package org.example.socialmediaapp.service.implementation;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.socialmediaapp.dto.UserDto;
import org.example.socialmediaapp.dto.req.CreateCommentRequest;

import org.example.socialmediaapp.dto.res.CommentResponse;
import org.example.socialmediaapp.entity.Account;
import org.example.socialmediaapp.entity.Comment;
import org.example.socialmediaapp.entity.Post;
import org.example.socialmediaapp.integration.minio.MinioChannel;
import org.example.socialmediaapp.repository.AccountRepository;
import org.example.socialmediaapp.repository.CommentRepository;
import org.example.socialmediaapp.repository.CommentVoteRepository;
import org.example.socialmediaapp.repository.PostRepository;
import org.example.socialmediaapp.service.CommentService;
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
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final AccountRepository accountRepository;
    private final PostRepository postRepository;
    private final MinioChannel minioChannel;
    private final CommentVoteRepository commentVoteRepository;

    @Override
    public CommentResponse createComment(CreateCommentRequest req, Long postId) {
        Account user = getCurrentAccount();

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Không tìm thấy bài viết với id: " + postId
                ));

        validateCommentRequest(req);

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setUser(user);
        comment.setContent(req.getContent());

        String imageUrl = null;

        MultipartFile file = req.getFile();
        if (file != null && !file.isEmpty()) {
            try (InputStream is = file.getInputStream()) {

                Map<String, String> upload = minioChannel.uploadFile(
                        file.getOriginalFilename(),
                        is,
                        file.getSize(),
                        file.getContentType(),
                        86400  // TTL 1 ngày
                );

                String objectKey = upload.get("objectKey");
                imageUrl = upload.get("url");   // dùng key "url"

                comment.setObjectKey(objectKey);

            } catch (Exception e) {
                throw new RuntimeException("Không thể upload ảnh bình luận", e);
            }
        }

        // lưu comment
        commentRepository.save(comment);

        // tăng số comment của post
        post.incrementCommentCount();
        postRepository.save(post);

        return CommentResponse.builder()
                .id(comment.getId())
                .user(buildUserDto(user))
                .content(comment.getContent())
                .imageUrl(imageUrl)
                .voteCount(comment.getVoteCount())
                .userVoted(false)
                .createdAt(comment.getCreatedAt())
                .build();
    }

    @Override
    public void deleteComment(Long id) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Comment comment = commentRepository.findById(id)
                .orElseThrow(()->new EntityNotFoundException("Không tìm thấy bình luận với id:" + id));

        Post post = comment.getPost();

        if(!userId.equals(comment.getUser().getId())) {
            throw new AccessDeniedException("Bạn không có quyền xoá bình luận này");
        }

        post.decreaseCommentCount(comment.getReplyCount() + 1);
        commentRepository.delete(comment);
        postRepository.save(post);
    }

    @Override
    public List<CommentResponse> getAllComments(Long postId){
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        List<Comment> comments = commentRepository.findCommentsWithUser(postId);

        List<Long> votedIds = commentVoteRepository.findVotedCommentIds(userId, postId);
        Set<Long> votedSet = new HashSet<>(votedIds);

        return comments.stream()
                .map(comment -> CommentResponse.builder()
                        .id(comment.getId())
                        .user(buildUserDto(comment.getUser()))
                        .content(comment.getContent())
                        .imageUrl(minioChannel.getPresignedUrlSafe(comment.getObjectKey(), 86400))
                        .voteCount(comment.getVoteCount())
                        .userVoted(votedSet.contains(comment.getId()))
                        .createdAt(comment.getCreatedAt())
                        .build())
                .toList();
    }

    private UserDto buildUserDto(Account user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                minioChannel.getPresignedUrlSafe(user.getObject_key(), 86400)
        );
    }

    private void validateCommentRequest(CreateCommentRequest req) {

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
