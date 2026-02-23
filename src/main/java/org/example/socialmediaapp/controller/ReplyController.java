package org.example.socialmediaapp.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.socialmediaapp.dto.req.CreateCommentRequest;
import org.example.socialmediaapp.dto.res.ApiResponse;
import org.example.socialmediaapp.dto.res.ReplyResponse;
import org.example.socialmediaapp.service.ReplyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reply")
@RequiredArgsConstructor
public class ReplyController {

    private final ReplyService replyService;

    @PostMapping(
            value = "/{commentId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<ReplyResponse>> createReplyComment(
            @ModelAttribute CreateCommentRequest request,
            @PathVariable Long commentId,
            @RequestParam(required = false) Long replyParentId
    ) {
        ReplyResponse replyResponse = replyService.createReplyComment(request, commentId, replyParentId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.<ReplyResponse>builder()
                                .code(HttpServletResponse.SC_CREATED)
                                .message("Trả lời bình luận thành công")
                                .data(replyResponse)
                                .build()
                );
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<ApiResponse<List<ReplyResponse>>> getAllRepliesByCommentId(
            @PathVariable Long commentId) {

        List<ReplyResponse> replies = replyService.getAllRepliesByCommentId(commentId);

        return ResponseEntity.ok(
                ApiResponse.<List<ReplyResponse>>builder()
                        .code(HttpServletResponse.SC_OK)
                        .message("Lấy danh sách bình luận thành công")
                        .data(replies)
                        .build()
        );
    }

    @DeleteMapping("/{replyId}")
    public ResponseEntity<ApiResponse<Void>> deleteReplyComment(
            @PathVariable Long replyId
    ){
        replyService.deleteReplyById(replyId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.<Void>builder()
                        .code(HttpServletResponse.SC_OK)
                        .message("Xóa phản hồi thành công")
                        .data(null)
                        .build());
    }
}
