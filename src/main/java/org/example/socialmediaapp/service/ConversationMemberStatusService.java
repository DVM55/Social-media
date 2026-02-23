package org.example.socialmediaapp.service;

import org.example.socialmediaapp.entity.Account;
import org.example.socialmediaapp.entity.Conversation;
import org.example.socialmediaapp.entity.ConversationMemberStatus;

public interface ConversationMemberStatusService {
    ConversationMemberStatus updateMessage(Conversation conversation, Account account, int unread, Long lastMessageId);

    void resetUnreadCount(Long conversationId, Long userId);
}
