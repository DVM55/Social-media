package org.example.socialmediaapp.repository;

import org.example.socialmediaapp.entity.ConversationMemberId;
import org.example.socialmediaapp.entity.ConversationMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationMemberStatusRepository extends JpaRepository<ConversationMemberStatus, ConversationMemberId> {
}
