package com.bus.monitoringsystem.api.bus.dto.result;

import com.bus.monitoringsystem.api.gps.model.GpsLocation;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class BusPathPointResult {

    private final BigDecimal latitude;
    private final BigDecimal longitude;
    private final LocalDateTime recordedAt;

    public static BusPathPointResult from(GpsLocation gps) {

        return BusPathPointResult.builder()
                .latitude(gps.getLatitude())
                .longitude(gps.getLongitude())
                .recordedAt(gps.getRecordedAt())
                .build();
    }
}
