package org.example.socialmediaapp.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReplyVoteId implements Serializable {
    private Long replyId;
    private Long userId;
}
