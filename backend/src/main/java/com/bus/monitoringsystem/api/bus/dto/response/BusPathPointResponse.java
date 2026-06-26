package com.bus.monitoringsystem.api.bus.dto.response;

import com.bus.monitoringsystem.api.bus.dto.result.BusPathPointResult;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class BusPathPointResponse {

    private final BigDecimal latitude;
    private final BigDecimal longitude;
    private final LocalDateTime recordedAt;

    public static BusPathPointResponse from(BusPathPointResult result) {

        return BusPathPointResponse.builder()
                .latitude(result.getLatitude())
                .longitude(result.getLongitude())
                .recordedAt(result.getRecordedAt())
                .build();
    }
}
