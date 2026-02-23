package org.example.socialmediaapp.dto.req;

import lombok.Data;
import org.example.socialmediaapp.enums.PostStatus;

import java.util.List;
@Data
public class CreatePostRequest {
    private String content;
    private PostStatus status;
    private List<PostMediaRequest> medias;
}
