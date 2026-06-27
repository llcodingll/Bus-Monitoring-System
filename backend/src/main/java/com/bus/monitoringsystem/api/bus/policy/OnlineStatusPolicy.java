package com.bus.monitoringsystem.api.bus.policy;

import com.bus.monitoringsystem.api.bus.model.BusStatus;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class OnlineStatusPolicy {

    private static final Duration THRESHOLD = Duration.ofMinutes(5);

    public BusStatus resolve(Instant lastCommunicatedAt, Instant now) {

        if (lastCommunicatedAt == null) {
            return BusStatus.OFFLINE;
        }
        Duration elapsed = Duration.between(lastCommunicatedAt, now);
        return elapsed.compareTo(THRESHOLD) <= 0 ? BusStatus.ONLINE : BusStatus.OFFLINE;
    }
}
