package org.example.socialmediaapp.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.socialmediaapp.dto.UserDto;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponse {
    private Long id;
    private UserDto user;
    private String content;
    private String imageUrl;
    private Integer voteCount;
    private Integer replyCount;
    private boolean userVoted;
    private LocalDateTime createdAt;
}
