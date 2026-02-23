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
public class ReplyResponse {
    private Long id;
    private UserDto user;
    private String content;
    private String imageUrl;
    private Integer voteCount;
    private boolean userVoted;
    private Long replyUserId;
    private String replyUserName;
    private LocalDateTime createdAt;
}
