package org.example.socialmediaapp.integration.minio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.socialmediaapp.enums.FileType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadResult {
    private String objectKey;
    private String uploadUrl;
    private FileType fileType;
    private String fileName;
}
