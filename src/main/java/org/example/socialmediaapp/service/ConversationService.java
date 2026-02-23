package org.example.socialmediaapp.service;

import org.example.socialmediaapp.dto.res.ConversationResponse;
import org.example.socialmediaapp.entity.Account;
import org.example.socialmediaapp.entity.Conversation;

import java.util.List;

public interface ConversationService {
    Conversation getOrCreateConversation(Account sender, Account receiver);

    List<ConversationResponse> getAllConversationsByUser();
}
