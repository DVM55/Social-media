package org.example.socialmediaapp.dto.req;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CreateCommentRequest {
    private String content;
    private MultipartFile file;
}
