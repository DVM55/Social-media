package org.example.socialmediaapp.service.implementation;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.socialmediaapp.dto.PostDto;
import org.example.socialmediaapp.dto.PostMediaDto;
import org.example.socialmediaapp.dto.UserDto;
import org.example.socialmediaapp.dto.req.CreatePostRequest;
import org.example.socialmediaapp.dto.req.PostMediaRequest;
import org.example.socialmediaapp.dto.res.PostResponse;
import org.example.socialmediaapp.entity.Account;
import org.example.socialmediaapp.entity.Post;
import org.example.socialmediaapp.entity.PostMedia;
import org.example.socialmediaapp.enums.PostStatus;
import org.example.socialmediaapp.integration.minio.MinioChannel;
import org.example.socialmediaapp.repository.*;
import org.example.socialmediaapp.service.PostService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final PostMediaRepository postMediaRepository;
    private final AccountRepository accountRepository;
    private final PostVoteRepository postVoteRepository;
    private final MinioChannel minioChannel;

    @Override
    public void createPost(CreatePostRequest request) {
        Account user = getCurrentAccount();
        validatePostRequest(request);

        Post post = Post.builder()
                .content(request.getContent())
                .status(defaultStatus(request.getStatus()))
                .user(user)
                .build();

        postRepository.save(post);
        savePostMedias(post, request.getMedias());
    }

    @Override
    public void sharePost(CreatePostRequest request, Long originalPostId) {
        Account user = getCurrentAccount();
        Post original = postRepository.findById(originalPostId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy bài viết với id: " + originalPostId));
        original.incrementShareCount();

        Post post = Post.builder()
                .content(request.getContent())
                .status(defaultStatus(request.getStatus()))
                .originalPost(original)
                .user(user)
                .build();

        postRepository.save(original);
        postRepository.save(post);
        savePostMedias(post, request.getMedias());
    }

    @Override
    public void deletePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy bài đăng với id: " + id));

        Long userId = currentUserId();
        if (!userId.equals(post.getUser().getId())) {
            throw new AccessDeniedException("Bạn không có quyền xoá bài viết này");
        }

        postRepository.delete(post);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostResponse> getAllPosts() {

        Long userId = currentUserId();

        // ---------------------------------------------------------------
        // 1) Load posts chính + user (Optimized JPQL)
        // ---------------------------------------------------------------
        List<Post> posts = postRepository.findAllPosts();

        if (posts.isEmpty()) {
            return List.of();
        }

        List<Long> postIds = posts.stream().map(Post::getId).toList();

        // ---------------------------------------------------------------
        // 2) Load media của posts theo batch
        // ---------------------------------------------------------------
        List<PostMedia> medias = postMediaRepository.findAllMediasByPostIds(postIds);

        Map<Long, List<PostMedia>> mediaMap =
                medias.stream()
                        .collect(Collectors.groupingBy(m -> m.getPost().getId()));

        // ---------------------------------------------------------------
        // 3) Load originalPosts theo batch
        // ---------------------------------------------------------------
        List<Long> originalIds = posts.stream()
                .map(Post::getOriginalPost)
                .filter(Objects::nonNull)
                .map(Post::getId)
                .distinct()
                .toList();

        Map<Long, Post> originalPostMap = originalIds.isEmpty()
                ? Collections.emptyMap()
                : postRepository.findOriginalPosts(originalIds)
                .stream()
                .collect(Collectors.toMap(Post::getId, p -> p));

        // ---------------------------------------------------------------
        // 4) Load originalPost.user theo batch
        // ---------------------------------------------------------------
        List<Long> originalUserIds = originalPostMap.values().stream()
                .map(p -> p.getUser().getId())
                .distinct()
                .toList();

        Map<Long, Account> originalUserMap = originalUserIds.isEmpty()
                ? Collections.emptyMap()
                : accountRepository.findUsersByIds(originalUserIds)
                .stream()
                .collect(Collectors.toMap(Account::getId, u -> u));

        // ---------------------------------------------------------------
        // 5) Load medias của originalPost theo batch
        // ---------------------------------------------------------------
        List<PostMedia> originalMediaList = originalIds.isEmpty()
                ? List.of()
                : postMediaRepository.findAllMediasByPostIds(originalIds);

        Map<Long, List<PostMedia>> originalMediaMap =
                originalMediaList.stream()
                        .collect(Collectors.groupingBy(m -> m.getPost().getId()));

        // ---------------------------------------------------------------
        // 6) Load voted
        // ---------------------------------------------------------------
        Set<Long> votedSet = new HashSet<>(postVoteRepository.findAllVotedPostIdsByUser(userId));

        // ---------------------------------------------------------------
        // 7) Build FEED RESPONSE
        // ---------------------------------------------------------------
        return posts.stream()
                .map(post -> PostResponse.builder()
                        .id(post.getId())
                        .content(post.getContent())
                        .createdAt(post.getCreatedAt())

                        // user
                        .user(buildUserDto(post.getUser()))

                        // media
                        .medias(buildMediaList(mediaMap.get(post.getId())))

                        // original post
                        .originalPost(buildOriginalPost(
                                post.getOriginalPost(),
                                originalPostMap,
                                originalUserMap,
                                originalMediaMap
                        ))

                        // stats
                        .voteCount(post.getVoteCount())
                        .commentCount(post.getCommentCount())
                        .shareCount(post.getShareCount())
                        .userVoted(votedSet.contains(post.getId()))

                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostResponse> getAllPostsByUser() {

        Long userId = currentUserId();

        // ---------------------------------------------------------------
        // 1) Load posts chính + user (Optimized JPQL)
        // ---------------------------------------------------------------
        List<Post> posts = postRepository.findAllPostsByUser(userId);

        if (posts.isEmpty()) {
            return List.of();
        }

        List<Long> postIds = posts.stream().map(Post::getId).toList();

        // ---------------------------------------------------------------
        // 2) Load media của posts theo batch
        // ---------------------------------------------------------------
        List<PostMedia> medias = postMediaRepository.findAllMediasByPostIds(postIds);

        Map<Long, List<PostMedia>> mediaMap =
                medias.stream()
                        .collect(Collectors.groupingBy(m -> m.getPost().getId()));

        // ---------------------------------------------------------------
        // 3) Load originalPosts theo batch
        // ---------------------------------------------------------------
        List<Long> originalIds = posts.stream()
                .map(Post::getOriginalPost)
                .filter(Objects::nonNull)
                .map(Post::getId)
                .distinct()
                .toList();

        Map<Long, Post> originalPostMap = originalIds.isEmpty()
                ? Collections.emptyMap()
                : postRepository.findOriginalPosts(originalIds)
                .stream()
                .collect(Collectors.toMap(Post::getId, p -> p));

        // ---------------------------------------------------------------
        // 4) Load originalPost.user theo batch
        // ---------------------------------------------------------------
        List<Long> originalUserIds = originalPostMap.values().stream()
                .map(p -> p.getUser().getId())
                .distinct()
                .toList();

        Map<Long, Account> originalUserMap = originalUserIds.isEmpty()
                ? Collections.emptyMap()
                : accountRepository.findUsersByIds(originalUserIds)
                .stream()
                .collect(Collectors.toMap(Account::getId, u -> u));

        // ---------------------------------------------------------------
        // 5) Load medias của originalPost theo batch
        // ---------------------------------------------------------------
        List<PostMedia> originalMediaList = originalIds.isEmpty()
                ? List.of()
                : postMediaRepository.findAllMediasByPostIds(originalIds);

        Map<Long, List<PostMedia>> originalMediaMap =
                originalMediaList.stream()
                        .collect(Collectors.groupingBy(m -> m.getPost().getId()));

        // ---------------------------------------------------------------
        // 6) Load voted
        // ---------------------------------------------------------------
        Set<Long> votedSet = new HashSet<>(postVoteRepository.findAllVotedPostIdsByUser(userId));

        // ---------------------------------------------------------------
        // 7) Build FEED RESPONSE
        // ---------------------------------------------------------------
        return posts.stream()
                .map(post -> PostResponse.builder()
                        .id(post.getId())
                        .content(post.getContent())
                        .createdAt(post.getCreatedAt())

                        // user
                        .user(buildUserDto(post.getUser()))

                        // media
                        .medias(buildMediaList(mediaMap.get(post.getId())))

                        // original post
                        .originalPost(buildOriginalPost(
                                post.getOriginalPost(),
                                originalPostMap,
                                originalUserMap,
                                originalMediaMap
                        ))

                        // stats
                        .voteCount(post.getVoteCount())
                        .commentCount(post.getCommentCount())
                        .shareCount(post.getShareCount())
                        .userVoted(votedSet.contains(post.getId()))

                        .build())
                .toList();
    }


    // ====================== Helper Methods =========================

    private void validatePostRequest(CreatePostRequest req) {
        boolean noContent = req.getContent() == null || req.getContent().trim().isEmpty();
        boolean noMedia = req.getMedias() == null || req.getMedias().isEmpty();

        if (noContent && noMedia) {
            throw new IllegalArgumentException("Bài đăng phải có nội dung");
        }
    }

    private void savePostMedias(Post post, List<PostMediaRequest> medias) {
        if (medias == null || medias.isEmpty()) return;

        medias.forEach(mediaReq -> {
            PostMedia media = PostMedia.builder()
                    .post(post)
                    .fileType(mediaReq.getFileType())
                    .objectKey(mediaReq.getObjectKey())
                    .mediaIndex(mediaReq.getMediaIndex())
                    .build();
            postMediaRepository.save(media);
        });
    }

    private PostStatus defaultStatus(PostStatus status) {
        return status != null ? status : PostStatus.PUBLIC;
    }

    private Long currentUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private UserDto buildUserDto(Account user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                minioChannel.getPresignedUrlSafe(user.getObject_key(), 86400)
        );
    }

    private List<PostMediaDto> buildMediaList(List<PostMedia> medias) {
        if (medias == null) return List.of();

        return medias.stream()
                .map(m -> PostMediaDto.builder()
                        .orderIndex(m.getMediaIndex())
                        .fileType(m.getFileType())
                        .fileUrl(minioChannel.getPresignedUrlSafe(m.getObjectKey(), 86400))
                        .build())
                .toList();
    }

    private PostDto buildOriginalPost(
            Post original,
            Map<Long, Post> originalPostMap,
            Map<Long, Account> originalUserMap,
            Map<Long, List<PostMedia>> originalMediaMap) {

        if (original == null) return null;

        Post op = originalPostMap.get(original.getId());
        if (op == null) return null;

        Account user = originalUserMap.get(op.getUser().getId());

        return PostDto.builder()
                .id(op.getId())
                .content(op.getContent())
                .createdAt(op.getCreatedAt())
                .user(buildUserDto(user))
                .medias(buildMediaList(originalMediaMap.get(op.getId())))
                .build();
    }

    private Account getCurrentAccount() {
        return accountRepository.findById(currentUserId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tài khoản"));
    }
}

