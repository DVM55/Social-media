package org.example.socialmediaapp.controller;

import lombok.RequiredArgsConstructor;
import org.example.socialmediaapp.dto.res.ApiResponse;
import org.example.socialmediaapp.service.CommentVoteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/comment_vote")
@RequiredArgsConstructor
public class CommentVoteController {
    private final CommentVoteService commentVoteService;

    @PostMapping("/toggle/{targetCommentId}")
    public ResponseEntity<ApiResponse<?>> toggleVoteComment (
            @PathVariable Long targetCommentId
    ){
        boolean vote = commentVoteService.toggleVoteComment(targetCommentId);
        String message = vote ? "Vote bình luận thành công" : "Bỏ vote bình luận thành công";

        ApiResponse<?> response = ApiResponse.builder()
                .code(vote ? 201 : 200)
                .message(message)
                .data(null)
                .build();

        return ResponseEntity
                .status(vote? HttpStatus.CREATED : HttpStatus.OK)
                .body(response);
    }
}
