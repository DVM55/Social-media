package org.example.socialmediaapp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "conversation_member_status")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationMemberStatus extends BaseEntity{
    @EmbeddedId
    private ConversationMemberId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("conversationId")
    @JsonIgnore
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("accountId")
    @JsonIgnore
    @JoinColumn(name = "account_id")
    private Account account;

    @Column(name = "unread_count", nullable = false, columnDefinition = "int default 0")
    @Builder.Default
    private Integer unreadCount = 0;

    @Column(name = "delete_until_message_id")
    private Long deleteUntilMessageId;

    @Column(name = "last_message_id")
    private Long lastMessageId;
}
