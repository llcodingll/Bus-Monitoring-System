package com.bus.monitoringsystem.api.event.dto.result;

import com.bus.monitoringsystem.api.event.model.Event;
import com.bus.monitoringsystem.api.event.model.EventType;
import com.bus.monitoringsystem.api.event.model.Severity;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class EventSummaryResult {

    private final Long id;
    private final Long busId;
    private final String busNumber;
    private final String routeNumber;
    private final String routeName;
    private final EventType eventType;
    private final Severity severity;
    private final BigDecimal latitude;
    private final BigDecimal longitude;
    private final LocalDateTime occurredAt;

    public static EventSummaryResult from(Event event) {

        return EventSummaryResult.builder()
                .id(event.getId())
                .busId(event.getBus().getId())
                .busNumber(event.getBus().getBusNumber())
                .routeNumber(event.getRoute().getRouteNumber())
                .routeName(event.getRoute().getRouteName())
                .eventType(event.getEventType())
                .severity(event.getSeverity())
                .latitude(event.getLatitude())
                .longitude(event.getLongitude())
                .occurredAt(event.getOccurredAt())
                .build();
    }
}
