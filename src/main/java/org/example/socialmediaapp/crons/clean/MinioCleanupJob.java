package org.example.socialmediaapp.crons.clean;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.socialmediaapp.crons.config.JobFactory;
import org.example.socialmediaapp.integration.minio.MinioChannel;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MinioCleanupJob {
    private final JobFactory jobFactory;
    private final MinioChannel minioChannel;

    @Scheduled(cron = "#{@jobFactory.getCronJobByName('cleanJob')}")
    public void run() {
        log.info("🚀 Start MinIO cleanJob");
        minioChannel.cleanupFile();
    }
}
