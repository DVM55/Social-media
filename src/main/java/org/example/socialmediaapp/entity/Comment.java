package org.example.socialmediaapp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "user_id", nullable = false)
    private Account user;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "object_key", columnDefinition = "TEXT")
    private String objectKey;

    @Builder.Default
    @Column(name = "vote_count")
    private Integer voteCount = 0;

    @Builder.Default
    @Column(name = "reply_count")
    private Integer replyCount=0;

    public void incrementVoteCount() {
        voteCount++;
    }

    public void incrementCommentCount() {
        replyCount++;
    }

    public void decrementVoteCount() {
        if(voteCount>0){
            voteCount--;
        }
    }

    public void decrementCommentCount() {
        if(replyCount>0){
            replyCount--;
        }
    }
}
