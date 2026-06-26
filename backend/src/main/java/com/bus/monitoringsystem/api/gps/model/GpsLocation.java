package com.bus.monitoringsystem.api.gps.model;

import com.bus.monitoringsystem.api.bus.model.Bus;
import com.bus.monitoringsystem.api.dispatch.model.BusDispatch;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "gps_locations",
        indexes = @Index(name = "idx_gps_bus_recorded", columnList = "bus_id, recorded_at DESC"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GpsLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_id", nullable = false)
    private Bus bus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispatch_id", nullable = false)
    private BusDispatch dispatch;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(nullable = false)
    private Integer speed;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @Builder
    public GpsLocation(Bus bus,
                       BusDispatch dispatch,
                       BigDecimal latitude,
                       BigDecimal longitude,
                       Integer speed,
                       LocalDateTime recordedAt) {

        this.bus = bus;
        this.dispatch = dispatch;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.recordedAt = recordedAt;
    }
}
