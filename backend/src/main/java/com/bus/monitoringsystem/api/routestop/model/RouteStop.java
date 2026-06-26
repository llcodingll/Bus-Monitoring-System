package com.bus.monitoringsystem.api.routestop.model;

import com.bus.monitoringsystem.api.route.model.Route;
import com.bus.monitoringsystem.api.stop.model.Stop;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "route_stops",
        uniqueConstraints = @UniqueConstraint(columnNames = {"route_id", "stop_order", "direction"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RouteStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stop_id", nullable = false)
    private Stop stop;

    @Column(name = "stop_order", nullable = false)
    private Integer stopOrder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Direction direction;

    @Builder
    public RouteStop(Route route, Stop stop, Integer stopOrder, Direction direction) {

        this.route = route;
        this.stop = stop;
        this.stopOrder = stopOrder;
        this.direction = direction;
    }
}
