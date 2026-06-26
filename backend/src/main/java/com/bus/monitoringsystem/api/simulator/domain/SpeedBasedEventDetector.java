package com.bus.monitoringsystem.api.simulator.domain;

import com.bus.monitoringsystem.api.event.model.EventType;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
public class SpeedBasedEventDetector {

    private static final double ACCELERATION_THRESHOLD_G = 0.3;
    private static final double G_MS2 = 9.81;
    private static final double STOPPED_THRESHOLD_KMH = 5.0;

    public Optional<EventType> detect(Double prevSpeedKmh, double currentSpeedKmh, Duration interval) {

        if (prevSpeedKmh == null || prevSpeedKmh < 0 || interval.isZero() || interval.isNegative()) {
            return Optional.empty();
        }

        double prevMs = prevSpeedKmh / 3.6;
        double currentMs = currentSpeedKmh / 3.6;
        double seconds = interval.toMillis() / 1000.0;
        double accelerationG = ((currentMs - prevMs) / seconds) / G_MS2;

        if (Math.abs(accelerationG) < ACCELERATION_THRESHOLD_G) {
            return Optional.empty();
        }

        if (accelerationG > 0) {
            return prevSpeedKmh <= STOPPED_THRESHOLD_KMH
                    ? Optional.of(EventType.SUDDEN_START)
                    : Optional.of(EventType.SUDDEN_ACCELERATION);
        } else {
            return currentSpeedKmh <= STOPPED_THRESHOLD_KMH
                    ? Optional.of(EventType.SUDDEN_BRAKE)
                    : Optional.of(EventType.SUDDEN_DECELERATION);
        }
    }
}
