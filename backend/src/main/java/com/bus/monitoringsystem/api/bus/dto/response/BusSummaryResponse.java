package com.bus.monitoringsystem.api.bus.dto.response;

import com.bus.monitoringsystem.api.bus.dto.result.BusSummaryResult;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class BusSummaryResponse {

    private final Long id;
    private final String busNumber;
    private final String routeNumber;
    private final String routeName;
    private final Integer currentSpeed;
    private final String status;
    private final String lastCommunicationAt;
    private final String currentStopName;
    private final String nextStopName;
    private final String direction;
    private final BigDecimal currentLatitude;
    private final BigDecimal currentLongitude;

    public static BusSummaryResponse from(BusSummaryResult result) {

        return BusSummaryResponse.builder()
                .id(result.getId())
                .busNumber(result.getBusNumber())
                .routeNumber(result.getRouteNumber())
                .routeName(result.getRouteName())
                .currentSpeed(result.getCurrentSpeed())
                .status(result.getStatus().name())
                .lastCommunicationAt(result.getLastCommunicationAt() != null
                        ? result.getLastCommunicationAt().toString() : null)
                .currentStopName(result.getCurrentStopName())
                .nextStopName(result.getNextStopName())
                .direction(result.getDirection() != null ? result.getDirection().name() : null)
                .currentLatitude(result.getCurrentLatitude())
                .currentLongitude(result.getCurrentLongitude())
                .build();
    }
}
