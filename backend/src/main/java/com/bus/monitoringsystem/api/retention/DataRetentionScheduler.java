package com.bus.monitoringsystem.api.retention;

import com.bus.monitoringsystem.api.gps.repository.GpsLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataRetentionScheduler {

    private static final int GPS_RETENTION_HOURS = 24;
    private static final int CHUNK_SIZE = 1000;

    private final GpsLocationRepository gpsLocationRepository;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void deleteOldGpsLocations() {

        LocalDateTime cutoff = LocalDateTime.now().minusHours(GPS_RETENTION_HOURS);
        int deleted;

        do {
            deleted = gpsLocationRepository.deleteChunkByRecordedAtBefore(cutoff);
        } while (deleted == CHUNK_SIZE);
    }
}
