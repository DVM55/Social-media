package org.example.socialmediaapp.dto.req;

import lombok.Data;
import org.example.socialmediaapp.enums.FileMediaType;

@Data
public class PostMediaRequest {
    private String objectKey;   // key trong MinIO
    private FileMediaType fileType;  // IMAGE, VIDEO
    private Integer mediaIndex; // vị trí
}
