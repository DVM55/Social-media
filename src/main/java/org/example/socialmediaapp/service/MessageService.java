package org.example.socialmediaapp.service;

import org.example.socialmediaapp.dto.req.SendFileRequest;
import org.example.socialmediaapp.dto.req.SendMessageRequest;
import org.example.socialmediaapp.dto.res.HistoryChatResponse;
import org.example.socialmediaapp.dto.res.MessageResponse;

import java.util.List;

public interface MessageService {
    MessageResponse sendMessage(Long receiverId, SendMessageRequest request);

    List<MessageResponse> sendFiles(Long receiverId, List<SendFileRequest> requests);

    HistoryChatResponse getLatestMessages(Long receiverId, int limit);

    HistoryChatResponse getOlderMessages(Long receiverId, Long lastMessageId, int limit);
}
