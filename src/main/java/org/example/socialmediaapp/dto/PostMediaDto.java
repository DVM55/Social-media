package org.example.socialmediaapp.dto;

import lombok.Builder;
import lombok.Data;
import org.example.socialmediaapp.enums.FileMediaType;

@Data
@Builder
public class PostMediaDto {
    private Integer orderIndex;
    private FileMediaType fileType;
    private String fileUrl;
}
