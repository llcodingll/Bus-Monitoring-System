package com.bus.monitoringsystem.api.gps.repository;

import com.bus.monitoringsystem.api.gps.model.GpsLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface GpsLocationRepository extends JpaRepository<GpsLocation, Long> {

    @Query("SELECT g FROM GpsLocation g WHERE g.bus.id = :busId ORDER BY g.recordedAt DESC LIMIT 50")
    List<GpsLocation> findTop50ByBusIdOrderByRecordedAtDesc(Long busId);

    @Modifying
    @Query(value = """
            DELETE FROM gps_locations
            WHERE id IN (
                SELECT id FROM gps_locations WHERE recorded_at < :cutoff LIMIT 1000
            )
            """, nativeQuery = true)
    int deleteChunkByRecordedAtBefore(LocalDateTime cutoff);
}
