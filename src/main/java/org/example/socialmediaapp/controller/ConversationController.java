package org.example.socialmediaapp.controller;

import lombok.RequiredArgsConstructor;
import org.example.socialmediaapp.dto.res.ApiResponse;
import org.example.socialmediaapp.dto.res.ConversationResponse;
import org.example.socialmediaapp.service.ConversationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/conversation")
@RequiredArgsConstructor
public class ConversationController {
    private final ConversationService conversationService;

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> getAllConversationByUser() {

        List<ConversationResponse> conversations = conversationService.getAllConversationsByUser();

        return ResponseEntity.ok(
                ApiResponse.<List<ConversationResponse>>builder()
                        .code(200)
                        .message("Lấy danh sách cuộc trò chuyện thành công!")
                        .data(conversations)
                        .build()
        );
    }

}
