package com.bus.monitoringsystem.api.simulator.domain;

import com.bus.monitoringsystem.api.routestop.model.Direction;
import org.springframework.stereotype.Component;

@Component
public class RouteProgressTracker {

    public NextStop nextStop(int currentStopOrder, Direction currentDirection, int maxStopOrderForDirection) {

        if (maxStopOrderForDirection <= 0) {
            throw new IllegalStateException("정류장이 없는 노선입니다.");
        }

        if (currentStopOrder >= maxStopOrderForDirection) {
            Direction reversed = currentDirection == Direction.OUTBOUND ? Direction.INBOUND : Direction.OUTBOUND;
            return new NextStop(1, reversed);
        }

        return new NextStop(currentStopOrder + 1, currentDirection);
    }
}
