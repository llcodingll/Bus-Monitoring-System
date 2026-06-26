package com.bus.monitoringsystem.api.event.dto.response;

import com.bus.monitoringsystem.api.event.dto.result.EventSummaryResult;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class EventSummaryResponse {

    private final Long id;
    private final Long busId;
    private final String busNumber;
    private final String routeNumber;
    private final String routeName;
    private final String eventType;
    private final String severity;
    private final BigDecimal latitude;
    private final BigDecimal longitude;
    private final LocalDateTime occurredAt;

    public static EventSummaryResponse from(EventSummaryResult result) {

        return EventSummaryResponse.builder()
                .id(result.getId())
                .busId(result.getBusId())
                .busNumber(result.getBusNumber())
                .routeNumber(result.getRouteNumber())
                .routeName(result.getRouteName())
                .eventType(result.getEventType().name())
                .severity(result.getSeverity().name())
                .latitude(result.getLatitude())
                .longitude(result.getLongitude())
                .occurredAt(result.getOccurredAt())
                .build();
    }
}
