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
public class CommentVoteId implements Serializable {
    private Long commentId;
    private Long userId;
}
