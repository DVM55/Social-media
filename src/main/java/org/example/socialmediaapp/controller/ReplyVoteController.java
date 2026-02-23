package org.example.socialmediaapp.controller;

import lombok.RequiredArgsConstructor;
import org.example.socialmediaapp.dto.res.ApiResponse;
import org.example.socialmediaapp.service.ReplyVoteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reply_vote")
@RequiredArgsConstructor
public class ReplyVoteController {
    private final ReplyVoteService replyVoteService;

    @PostMapping("/toggle/{targetReplyId}")
    public ResponseEntity<ApiResponse<?>> toggleVoteReply (
            @PathVariable Long targetReplyId
    ){
        boolean vote = replyVoteService.toggleVoteReply(targetReplyId);
        String message = vote ? "Vote phản hồi thành công" : "Bỏ vote phản hồi thành công";

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
