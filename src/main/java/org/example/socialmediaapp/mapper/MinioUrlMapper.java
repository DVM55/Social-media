package org.example.socialmediaapp.mapper;

import org.example.socialmediaapp.integration.minio.MinioChannel;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MinioUrlMapper {

    private static MinioChannel minioChannel;

    @Autowired
    public MinioUrlMapper(MinioChannel channel) {
        MinioUrlMapper.minioChannel = channel;
    }

    @Named("toPresignedUrl")
    public static String toPresignedUrl(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) return null;
        try {
            return minioChannel.presignedGetUrl(objectKey, 86400); // TTL 24h
        } catch (Exception e) {
            return null;
        }
    }
}
