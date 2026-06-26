package com.bus.monitoringsystem.api.event.model;

import com.bus.monitoringsystem.api.bus.model.Bus;
import com.bus.monitoringsystem.api.dispatch.model.BusDispatch;
import com.bus.monitoringsystem.api.route.model.Route;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "events",
        indexes = @Index(name = "idx_events_bus_occurred", columnList = "bus_id, occurred_at DESC"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_id", nullable = false)
    private Bus bus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispatch_id", nullable = false)
    private BusDispatch dispatch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Builder
    public Event(Bus bus,
                 BusDispatch dispatch,
                 Route route,
                 EventType eventType,
                 Severity severity,
                 BigDecimal latitude,
                 BigDecimal longitude,
                 LocalDateTime occurredAt) {

        this.bus = bus;
        this.dispatch = dispatch;
        this.route = route;
        this.eventType = eventType;
        this.severity = severity;
        this.latitude = latitude;
        this.longitude = longitude;
        this.occurredAt = occurredAt;
    }
}
