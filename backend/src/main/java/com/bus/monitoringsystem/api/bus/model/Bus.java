package com.bus.monitoringsystem.api.bus.model;

import com.bus.monitoringsystem.api.stop.model.Stop;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "buses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bus_number", nullable = false, unique = true)
    private String busNumber;

    @Column(name = "current_speed", nullable = false)
    private Integer currentSpeed;

    @Column(name = "current_latitude", precision = 10, scale = 7)
    private BigDecimal currentLatitude;

    @Column(name = "current_longitude", precision = 10, scale = 7)
    private BigDecimal currentLongitude;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_stop_id")
    private Stop currentStop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "next_stop_id")
    private Stop nextStop;

    @Column(name = "last_communication_at")
    private LocalDateTime lastCommunicationAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public Bus(String busNumber,
               Integer currentSpeed,
               BigDecimal currentLatitude,
               BigDecimal currentLongitude,
               Stop currentStop,
               Stop nextStop,
               LocalDateTime lastCommunicationAt) {

        this.busNumber = busNumber;
        this.currentSpeed = currentSpeed != null ? currentSpeed : 0;
        this.currentLatitude = currentLatitude;
        this.currentLongitude = currentLongitude;
        this.currentStop = currentStop;
        this.nextStop = nextStop;
        this.lastCommunicationAt = lastCommunicationAt;
        this.createdAt = LocalDateTime.now();
    }
}
