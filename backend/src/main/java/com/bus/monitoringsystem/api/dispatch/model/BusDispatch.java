package com.bus.monitoringsystem.api.dispatch.model;

import com.bus.monitoringsystem.api.bus.model.Bus;
import com.bus.monitoringsystem.api.route.model.Route;
import com.bus.monitoringsystem.api.routestop.model.Direction;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bus_dispatches")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BusDispatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_id", nullable = false)
    private Bus bus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Direction direction;

    @Column(name = "dispatched_date", nullable = false)
    private LocalDate dispatchedDate;

    @Column(name = "operation_started_at", nullable = false)
    private LocalDateTime operationStartedAt;

    @Column(name = "operation_ended_at")
    private LocalDateTime operationEndedAt;

    @Builder
    public BusDispatch(Bus bus,
                       Route route,
                       Direction direction,
                       LocalDate dispatchedDate,
                       LocalDateTime operationStartedAt) {

        this.bus = bus;
        this.route = route;
        this.direction = direction;
        this.dispatchedDate = dispatchedDate;
        this.operationStartedAt = operationStartedAt;
        this.operationEndedAt = null;
    }
}
