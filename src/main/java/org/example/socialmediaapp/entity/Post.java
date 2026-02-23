package org.example.socialmediaapp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.example.socialmediaapp.enums.PostStatus;

import java.util.List;

@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    @Enumerated(EnumType.STRING)
    private PostStatus status =PostStatus.PUBLIC;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "user_id", nullable = false)
    private Account user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "original_post_id")
    private Post originalPost;

    @Builder.Default
    @Column(name = "vote_count")
    private Integer voteCount = 0;

    @Builder.Default
    @Column(name = "comment_count")
    private Integer commentCount = 0;

    @Builder.Default
    @Column(name = "share_count")
    private Integer shareCount = 0;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<PostMedia> medias;

    public void incrementVoteCount() {
        voteCount++;
    }

    public void incrementCommentCount() {
        commentCount++;
    }

    public void incrementShareCount() {
        shareCount++;
    }

    public void decrementVoteCount() {
        if (voteCount > 0) {
            voteCount--;
        }
    }

    public void decrementCommentCount() {
        if (commentCount > 0) {
            commentCount--;
        }
    }

    public void decrementShareCount() {
        if (shareCount > 0) {
            shareCount--;
        }
    }

    public void decreaseCommentCount(int decrement) {
        if(voteCount>decrement){
            voteCount-=decrement;
        }
    }

}
