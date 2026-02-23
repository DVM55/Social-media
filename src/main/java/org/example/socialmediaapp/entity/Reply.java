package org.example.socialmediaapp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "replies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reply extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "user_id", nullable = false)
    private Account user;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "object_key", columnDefinition = "TEXT")
    private String objectKey;

    @ManyToOne
    @JoinColumn(name = "reply_user_id")
    @JsonIgnore
    private Account replyUser;

    @Builder.Default
    @Column(name = "vote_count")
    private Integer voteCount = 0;

    public void incrementVoteCount() {
        voteCount++;
    }

    public void decrementVoteCount() {
        if (voteCount > 0) {
            voteCount--;
        }
    }
}
