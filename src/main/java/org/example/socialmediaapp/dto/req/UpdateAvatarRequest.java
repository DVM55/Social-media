package org.example.socialmediaapp.dto.req;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UpdateAvatarRequest {
    private MultipartFile file;
}
