package com.bus.monitoringsystem.api.bus.dto.result;

import com.bus.monitoringsystem.api.bus.model.BusStatus;
import com.bus.monitoringsystem.api.routestop.model.Direction;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class BusSummaryResult {

    private final Long id;
    private final String busNumber;
    private final String routeNumber;
    private final String routeName;
    private final Integer currentSpeed;
    private final BusStatus status;
    private final LocalDateTime lastCommunicationAt;
    private final String currentStopName;
    private final String nextStopName;
    private final Direction direction;
    private final BigDecimal currentLatitude;
    private final BigDecimal currentLongitude;
}
