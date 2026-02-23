package org.example.socialmediaapp.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.socialmediaapp.dto.PostDto;
import org.example.socialmediaapp.dto.PostMediaDto;
import org.example.socialmediaapp.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponse {
    private Long id;
    private String content;
    private UserDto user;
    private Integer voteCount;
    private Integer commentCount;
    private Integer shareCount;
    private LocalDateTime createdAt;
    private List<PostMediaDto> medias;
    private PostDto originalPost;
    private boolean userVoted;
}
