package org.example.socialmediaapp.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.socialmediaapp.dto.req.SendFileRequest;
import org.example.socialmediaapp.dto.req.SendMessageRequest;
import org.example.socialmediaapp.dto.res.ApiResponse;
import org.example.socialmediaapp.dto.res.HistoryChatResponse;
import org.example.socialmediaapp.dto.res.MessageResponse;
import org.example.socialmediaapp.service.MessageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/send/{receiverId}")
    public ResponseEntity<ApiResponse<MessageResponse>> register(
            @Valid @RequestBody SendMessageRequest request,
            @PathVariable Long receiverId
    ) {
        MessageResponse response = messageService.sendMessage(receiverId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<MessageResponse>builder()
                        .code(HttpServletResponse.SC_CREATED)
                        .message("Gửi tin nhắn thành công!")
                        .data(response)
                        .build());
    }

    @PostMapping("/send-files/{receiverId}")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> sendFiles(
            @Valid @RequestBody List<SendFileRequest> requests,
            @PathVariable Long receiverId
    ) {
        List<MessageResponse> responses = messageService.sendFiles(receiverId, requests);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<List<MessageResponse>>builder()
                        .code(HttpServletResponse.SC_CREATED)
                        .message("Gửi file thành công!")
                        .data(responses)
                        .build());
    }

    @GetMapping("/latest/{receiverId}")
    public ResponseEntity<ApiResponse<HistoryChatResponse>> getLatestMessages(
            @PathVariable Long receiverId,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        HistoryChatResponse response = messageService.getLatestMessages(receiverId, limit);
        return ResponseEntity.ok(
                ApiResponse.<HistoryChatResponse>builder()
                        .code(HttpServletResponse.SC_OK)
                        .message("Lấy lịch sử tin nhắn thành công!")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/older/{receiverId}")
    public ResponseEntity<ApiResponse<HistoryChatResponse>> getOlderMessages(
            @PathVariable Long receiverId,
            @RequestParam Long lastMessageId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        HistoryChatResponse response = messageService.getOlderMessages(receiverId, lastMessageId, limit);
        return ResponseEntity.ok(
                ApiResponse.<HistoryChatResponse>builder()
                        .code(HttpServletResponse.SC_OK)
                        .message("Lấy lịch sử tin nhắn thành công!")
                        .data(response)
                        .build()
        );
    }
}
