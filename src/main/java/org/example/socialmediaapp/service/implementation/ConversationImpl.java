package org.example.socialmediaapp.service.implementation;

import lombok.RequiredArgsConstructor;
import org.example.socialmediaapp.dto.MessageDto;
import org.example.socialmediaapp.dto.UserDto;
import org.example.socialmediaapp.dto.res.ConversationResponse;
import org.example.socialmediaapp.entity.*;
import org.example.socialmediaapp.integration.minio.MinioChannel;
import org.example.socialmediaapp.repository.ConversationRepository;
import org.example.socialmediaapp.repository.MessageRepository;
import org.example.socialmediaapp.service.ConversationService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConversationImpl implements ConversationService {

    private final ConversationRepository conversationRepository;
    private final MinioChannel minioChannel;
    private final MessageRepository messageRepository;

    @Override
    @Transactional
    public Conversation getOrCreateConversation(Account sender, Account receiver){
        return conversationRepository
                .findDirectConversationBetween(sender.getId(), receiver.getId())
                .orElseGet(() -> {
                    Conversation c = new Conversation();

                    addMember(c, sender);
                    addMember(c, receiver);
                    
                    return conversationRepository.save(c);
                });
    }

    @Override
    public List<ConversationResponse> getAllConversationsByUser() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Conversation> conversations = conversationRepository.findAllByAccount(userId);

        return conversations.stream().map(conversation -> {
            ConversationMemberStatus myStatus = conversation.getMemberStatus()
                    .stream()
                    .filter(ms -> ms.getAccount().getId().equals(userId))
                    .findFirst()
                    .orElseThrow();

            Account partner = conversation.getMemberStatus()
                    .stream()
                    .map(ConversationMemberStatus::getAccount)
                    .filter(acc -> !acc.getId().equals(userId))
                    .findFirst()
                    .orElseThrow();
            UserDto partnerDto = buildUserDto(partner);

            Message lastMessage = messageRepository.findById(myStatus.getLastMessageId())
                    .orElseThrow();

            MessageDto lastMessageDto = buildMessageDto(lastMessage);

            return new ConversationResponse(
                    conversation.getId(),
                    partnerDto,
                    lastMessageDto,
                    myStatus.getUnreadCount()
            );

        }).toList();
    }


    private UserDto buildUserDto(Account user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                minioChannel.getPresignedUrlSafe(user.getObject_key(), 86400)
        );
    }

    private MessageDto buildMessageDto(Message message) {
        return new MessageDto(
                message.getId(),
                message.getMessageType(),
                message.getContent(),
                message.getFileName(),
                minioChannel.getPresignedUrlSafe(message.getObjectKey(), 86400),
                message.getCreatedAt()
        );
    }

    private void addMember(Conversation conversation, Account account) {
        ConversationMemberStatus member = new ConversationMemberStatus();
        member.setConversation(conversation);
        member.setAccount(account);
        member.setId(new ConversationMemberId(
                conversation.getId(),
                account.getId()
        ));
        conversation.getMemberStatus().add(member);
    }

}
