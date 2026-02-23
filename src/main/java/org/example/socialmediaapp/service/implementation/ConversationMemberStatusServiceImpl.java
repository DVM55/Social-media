package org.example.socialmediaapp.service.implementation;

import lombok.RequiredArgsConstructor;
import org.example.socialmediaapp.entity.Account;
import org.example.socialmediaapp.entity.Conversation;
import org.example.socialmediaapp.entity.ConversationMemberId;
import org.example.socialmediaapp.entity.ConversationMemberStatus;
import org.example.socialmediaapp.repository.ConversationMemberStatusRepository;
import org.example.socialmediaapp.service.ConversationMemberStatusService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConversationMemberStatusServiceImpl implements ConversationMemberStatusService {
    private final ConversationMemberStatusRepository conversationMemberStatusRepository;

    @Override
    @Transactional
    public ConversationMemberStatus updateMessage(Conversation conversation, Account account, int unread, Long lastMessageId){
        ConversationMemberId id = new ConversationMemberId(conversation.getId(), account.getId());

        ConversationMemberStatus memberStatus = conversationMemberStatusRepository.findById(id)
                .orElseGet(() -> {
                    ConversationMemberStatus newMemberStatus = new ConversationMemberStatus();
                    newMemberStatus.setId(new ConversationMemberId(
                            conversation.getId(),
                            account.getId()
                    ));
                    newMemberStatus.setConversation(conversation);
                    newMemberStatus.setAccount(account);
                    return newMemberStatus;
                });

        memberStatus.setUnreadCount(memberStatus.getUnreadCount() + unread);
        memberStatus.setLastMessageId(lastMessageId);

        return conversationMemberStatusRepository.save(memberStatus);
    }

    @Override
    public void resetUnreadCount(Long conversationId, Long userId) {
        ConversationMemberId id = new ConversationMemberId(conversationId, userId);

        conversationMemberStatusRepository.findById(id)
                .ifPresent(unread -> {
                    unread.setUnreadCount(0);
                    conversationMemberStatusRepository.save(unread);
                });
    }


}
