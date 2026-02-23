package org.example.socialmediaapp.controller;

import lombok.RequiredArgsConstructor;
import org.example.socialmediaapp.dto.res.AccountResponse;
import org.example.socialmediaapp.dto.res.ApiResponse;
import org.example.socialmediaapp.dto.res.PagingResponse;
import org.example.socialmediaapp.service.FollowService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/follow")
@RequiredArgsConstructor
public class FollowController {
    private final FollowService followService;

    @PostMapping("/toggle/{targetAccountId}")
    public ResponseEntity<ApiResponse<?>> toggleFollow(
            @PathVariable Long targetAccountId
    ){
        boolean follow = followService.toggleFollow(targetAccountId);
        String message = follow ? "Theo dõi thành công" : "Bỏ theo dõi thành công";

        ApiResponse<?> response = ApiResponse.builder()
                .code(follow ? 201 : 200)
                .message(message)
                .data(null)
                .build();

        // Trả về 201 nếu follow, 200 nếu unfollow
        return ResponseEntity
                .status(follow ? HttpStatus.CREATED : HttpStatus.OK)
                .body(response);
    }

    @GetMapping("/{targetAccountId}/followers")
    public ResponseEntity<PagingResponse<AccountResponse>> getFollowers(
            @PathVariable Long targetAccountId,
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "") String keyword
    ) {
        PagingResponse<AccountResponse> response = followService.queryFollowersOfUser(
                targetAccountId,
                lastId,
                limit,
                keyword
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{targetAccountId}/followings")
    public ResponseEntity<PagingResponse<AccountResponse>> getFollowings(
            @PathVariable Long targetAccountId,
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "") String keyword
    ) {
        PagingResponse<AccountResponse> response = followService.queryFollowingsOfUser(
                targetAccountId,
                lastId,
                limit,
                keyword
        );
        return ResponseEntity.ok(response);
    }
}
