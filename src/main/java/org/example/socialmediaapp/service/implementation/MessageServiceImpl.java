package org.example.socialmediaapp.service.implementation;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.socialmediaapp.dto.ChatDto;
import org.example.socialmediaapp.dto.MessageDto;
import org.example.socialmediaapp.dto.UserDto;
import org.example.socialmediaapp.dto.req.SendFileRequest;
import org.example.socialmediaapp.dto.req.SendMessageRequest;
import org.example.socialmediaapp.dto.res.HistoryChatResponse;
import org.example.socialmediaapp.dto.res.MessageResponse;
import org.example.socialmediaapp.entity.Account;
import org.example.socialmediaapp.entity.Conversation;
import org.example.socialmediaapp.entity.ConversationMemberStatus;
import org.example.socialmediaapp.entity.Message;
import org.example.socialmediaapp.enums.MessageType;
import org.example.socialmediaapp.integration.minio.MinioChannel;
import org.example.socialmediaapp.repository.AccountRepository;
import org.example.socialmediaapp.repository.ConversationRepository;
import org.example.socialmediaapp.repository.MessageRepository;
import org.example.socialmediaapp.service.ConversationMemberStatusService;
import org.example.socialmediaapp.service.ConversationService;
import org.example.socialmediaapp.service.MessageService;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final AccountRepository accountRepository;
    private final ConversationService conversationService;
    private final ConversationMemberStatusService conversationMemberStatusService;
    private final MinioChannel minioChannel;
    private final SimpMessagingTemplate messagingTemplate;
    private final ConversationRepository conversationRepository;

    @Override
    @Transactional
    public MessageResponse sendMessage(Long receiverId, SendMessageRequest request){
        Account sender = getCurrentUser();
        Account receiver = getUserById(receiverId);

        validateNotSameUser(sender.getId(), receiverId);

        Conversation conversation = conversationService.getOrCreateConversation(sender, receiver);

        Message message = Message.builder()
                .conversation(conversation)
                .account(sender)
                .messageType(MessageType.MESSAGE)
                .content(request.getContent())
                .build();
        Message savedMessage = messageRepository.save(message);

        UserDto userDto = buildUserDto(sender);
        MessageDto messageDto = buildMessageDto(savedMessage);

        ConversationMemberStatus receiverStatus = conversationMemberStatusService
                .updateMessage(conversation, receiver, 1, savedMessage.getId());

        MessageResponse response = MessageResponse.builder()
                .conversationId(conversation.getId())
                .sender(userDto)
                .message(messageDto)
                .unRead(receiverStatus.getUnreadCount())
                .build();

        messagingTemplate.convertAndSendToUser(
                String.valueOf(receiver.getId()),
                "/queue/messages",
                response
        );

        ConversationMemberStatus senderStatus = conversationMemberStatusService
                .updateMessage(conversation, sender, 0, savedMessage.getId());

        return MessageResponse.builder()
                .conversationId(conversation.getId())
                .sender(userDto)
                .message(messageDto)
                .unRead(senderStatus.getUnreadCount())
                .build();
    }

    @Override
    @Transactional
    public List<MessageResponse> sendFiles(Long receiverId, List<SendFileRequest> requests){
        Account sender = getCurrentUser();
        Account receiver = getUserById(receiverId);

        validateNotSameUser(sender.getId(), receiverId);

        Conversation conversation = conversationService.getOrCreateConversation(sender, receiver);

        UserDto senderDto = buildUserDto(sender);

        List<Message> messagesToSave = new ArrayList<>();

        for (SendFileRequest req : requests) {

            Message msg = Message.builder()
                    .conversation(conversation)
                    .account(sender)
                    .messageType(req.getMessageType())
                    .fileName(req.getFileName())
                    .objectKey(req.getObjectKey())
                    .build();

            messagesToSave.add(msg);
        }

        List<Message> savedMessages = messageRepository.saveAll(messagesToSave);

        Message lastMsg = savedMessages.getLast();

        ConversationMemberStatus receiverStatus =
                conversationMemberStatusService.updateMessage(
                        conversation,
                        receiver,
                        requests.size(),
                        lastMsg.getId()
                );

        ConversationMemberStatus senderStatus = conversationMemberStatusService
                .updateMessage(conversation, sender, 0, lastMsg.getId());

        int unread = receiverStatus.getUnreadCount() - savedMessages.size() + 1;

        List<MessageResponse> receiverResponses = new ArrayList<>();

        List<MessageResponse> senderResponses = new ArrayList<>();

        for (Message m : savedMessages) {
            receiverResponses.add(
                    MessageResponse.builder()
                            .conversationId(conversation.getId())
                            .sender(senderDto)
                            .message(buildMessageDto(m))
                            .unRead(unread++)
                            .build()
            );
            senderResponses.add(
                    MessageResponse.builder()
                            .conversationId(conversation.getId())
                            .sender(senderDto)
                            .message(buildMessageDto(m))
                            .unRead(senderStatus.getUnreadCount())
                            .build()
            );
        }

        messagingTemplate.convertAndSendToUser(
                String.valueOf(receiver.getId()),
                "/queue/message_files",
                receiverResponses
        );

        return senderResponses;
    }

    @Override
    public HistoryChatResponse getLatestMessages(Long receiverId, int limit) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        ConversationMeta meta = getConversationMeta(userId, receiverId);
        if (meta == null) return new HistoryChatResponse(null, false, Collections.emptyList());

        return loadMessages(
                meta.conversationId(),
                meta.deleteUntilMessageId(),
                meta.lastMessageId(),
                limit
        );
    }


    @Override
    public HistoryChatResponse getOlderMessages(Long receiverId, Long lastMessageId, int limit) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        ConversationMeta meta = getConversationMeta(userId, receiverId);
        if (meta == null) return new HistoryChatResponse(null, false, Collections.emptyList());

        return loadMessages(
                meta.conversationId(),
                meta.deleteUntilMessageId(),
                lastMessageId,
                limit
        );
    }


    private HistoryChatResponse loadMessages(Long conversationId,
                                             Long deleteUntilMessageId,
                                             Long lastMessageId,
                                             int limit) {

        List<Message> messages = messageRepository
                .findLatestMessages(conversationId, deleteUntilMessageId, lastMessageId, limit);

        boolean hasMore = !messages.isEmpty()
                && messageRepository.existsByConversationIdAndIdLessThan(
                conversationId,
                messages.getLast().getId()
        );

        Collections.reverse(messages);

        return new HistoryChatResponse(conversationId, hasMore, buildChatDtos(messages));
    }

    private List<ChatDto> buildChatDtos(List<Message> messages) {
        return messages.stream()
                .map(message -> new ChatDto(
                        message.getAccount().getId(),
                        buildMessageDto(message)
                )).toList();
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

    private Account getCurrentUser() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return accountRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng với id:" +userId));
    }

    private Account getUserById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng với id:" +id));
    }

    private void validateNotSameUser(Long senderId, Long receiverId) {
        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("Người gửi và người nhận không được trùng nhau");
        }
    }

    private ConversationMeta getConversationMeta(Long userId, Long receiverId) {
        Optional<Map<String, Object>> result = conversationRepository
                .findDirectConversationWithLastMessageAndDeleteUntil(userId, receiverId);

        if (result.isEmpty()) return null;

        Map<String, Object> row = result.get();

        Long conversationId = ((Number) row.get("conversationId")).longValue();
        Long lastMessageId = row.get("lastMessageId") != null
                ? ((Number) row.get("lastMessageId")).longValue()
                : null;
        Long deleteUntilMessageId = row.get("deleteUntilMessageId") != null
                ? ((Number) row.get("deleteUntilMessageId")).longValue()
                : null;

        return new ConversationMeta(conversationId, lastMessageId, deleteUntilMessageId);
    }


    private record ConversationMeta(Long conversationId,
                                    Long lastMessageId,
                                    Long deleteUntilMessageId) {}

}
