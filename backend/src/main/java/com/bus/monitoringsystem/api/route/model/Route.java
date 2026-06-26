package com.bus.monitoringsystem.api.route.model;

import com.bus.monitoringsystem.api.stop.model.Stop;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "routes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "route_number", nullable = false)
    private String routeNumber;

    @Column(name = "route_name", nullable = false)
    private String routeName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "start_stop_id")
    private Stop startStop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "end_stop_id")
    private Stop endStop;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public Route(String routeNumber, String routeName, Stop startStop, Stop endStop) {

        this.routeNumber = routeNumber;
        this.routeName = routeName;
        this.startStop = startStop;
        this.endStop = endStop;
        this.createdAt = LocalDateTime.now();
    }
}
