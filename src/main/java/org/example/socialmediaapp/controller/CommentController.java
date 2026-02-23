package org.example.socialmediaapp.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.socialmediaapp.dto.req.CreateCommentRequest;
import org.example.socialmediaapp.dto.res.ApiResponse;
import org.example.socialmediaapp.dto.res.CommentResponse;
import org.example.socialmediaapp.service.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping(
            value = "/{postId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @ModelAttribute CreateCommentRequest createCommentRequest,
            @PathVariable Long postId
    ) {

        CommentResponse commentResponse = commentService.createComment(createCommentRequest, postId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.<CommentResponse>builder()
                                .code(HttpServletResponse.SC_CREATED)
                                .message("Bình luận bài viết thành công")
                                .data(commentResponse)
                                .build()
                );
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getAllComments(
            @PathVariable Long postId) {

        List<CommentResponse> comments = commentService.getAllComments(postId);

        return ResponseEntity.ok(
                ApiResponse.<List<CommentResponse>>builder()
                        .code(HttpServletResponse.SC_OK)
                        .message("Lấy danh sách bình luận thành công")
                        .data(comments)
                        .build()
        );
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long commentId
    ){
        commentService.deleteComment(commentId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.<Void>builder()
                        .code(HttpServletResponse.SC_OK)
                        .message("Xóa bình luận thành công")
                        .data(null)
                        .build());
    }
}
