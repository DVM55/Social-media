package org.example.socialmediaapp.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.socialmediaapp.dto.req.CreatePostRequest;

import org.example.socialmediaapp.dto.res.ApiResponse;
import org.example.socialmediaapp.dto.res.PostResponse;
import org.example.socialmediaapp.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/post")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @PostMapping("")
    public ResponseEntity<ApiResponse<Void>> createPost(@Valid @RequestBody CreatePostRequest createPostRequest) {
        postService.createPost(createPostRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<Void>builder()
                        .code(HttpServletResponse.SC_CREATED)
                        .message("Tạo bài viết thành công!")
                        .data(null)
                        .build());
    }

    @PostMapping("/share/{originalPostId}")
    public ResponseEntity<ApiResponse<Void>> sharePost(
            @Valid @RequestBody CreatePostRequest createPostRequest,
            @PathVariable Long originalPostId)
    {
        postService.sharePost(createPostRequest, originalPostId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<Void>builder()
                        .code(HttpServletResponse.SC_CREATED)
                        .message("Chia sẻ bài viết thành công!")
                        .data(null)
                        .build());
    }

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<PostResponse>>> getAllPosts() {
        List<PostResponse> posts = postService.getAllPosts();
        return ResponseEntity.ok(
                ApiResponse.<List<PostResponse>>builder()
                        .code(HttpServletResponse.SC_OK)
                        .message("Lấy danh sách bài viết thành công")
                        .data(posts)
                        .build()
        );
    }

    // =====================================
    // LẤY TẤT CẢ BÀI VIẾT CỦA USER HIỆN TẠI
    // =====================================
    @GetMapping("/user")
    public ResponseEntity<ApiResponse<List<PostResponse>>> getAllPostsByUser() {
        List<PostResponse> posts = postService.getAllPostsByUser();
        return ResponseEntity.ok(
                ApiResponse.<List<PostResponse>>builder()
                        .code(HttpServletResponse.SC_OK)
                        .message("Lấy bài viết của người dùng thành công")
                        .data(posts)
                        .build()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .code(HttpServletResponse.SC_OK)
                        .message("Xóa bài viết thành công")
                        .data(null)
                        .build()
        );
    }
}
