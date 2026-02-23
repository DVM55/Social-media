package org.example.socialmediaapp.controller;

import lombok.RequiredArgsConstructor;
import org.example.socialmediaapp.dto.res.AccountResponse;
import org.example.socialmediaapp.dto.res.ApiResponse;
import org.example.socialmediaapp.dto.res.PagingResponse;
import org.example.socialmediaapp.service.PostVoteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/post_vote")
@RequiredArgsConstructor
public class PostVoteController {
    private final PostVoteService postVoteService;

    @PostMapping("/toggle/{targetPostId}")
    public ResponseEntity<ApiResponse<?>> toggleVotePost(
            @PathVariable Long targetPostId
    ){
        boolean vote = postVoteService.toggleVotePost(targetPostId);
        String message = vote ? "Vote bài viết thành công" : "Bỏ vote bài viết thành công";

        ApiResponse<?> response = ApiResponse.builder()
                .code(vote ? 201 : 200)
                .message(message)
                .data(null)
                .build();

        return ResponseEntity
                .status(vote? HttpStatus.CREATED : HttpStatus.OK)
                .body(response);
    }

    @GetMapping("/{targetPostId}")
    public ResponseEntity<PagingResponse<AccountResponse>> queryUserVote(
            @PathVariable Long targetPostId,
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "") String keyword
    ) {
        PagingResponse<AccountResponse> response = postVoteService.queryUserVote(
                targetPostId,
                lastId,
                limit,
                keyword
        );
        return ResponseEntity.ok(response);
    }
}
