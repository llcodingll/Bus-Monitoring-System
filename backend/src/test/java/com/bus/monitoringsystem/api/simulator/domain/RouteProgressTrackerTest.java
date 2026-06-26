package com.bus.monitoringsystem.api.simulator.domain;

import com.bus.monitoringsystem.api.routestop.model.Direction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("RouteProgressTracker")
class RouteProgressTrackerTest {

    private final RouteProgressTracker tracker = new RouteProgressTracker();

    @Test
    @DisplayName("중간 정류장에서는 다음 순서를 같은 방향으로 반환한다")
    void nextStop_returnsNextOrderSameDirection_whenMiddleStop() {

        // when
        NextStop result = tracker.nextStop(2, Direction.OUTBOUND, 5);

        // then
        assertThat(result.stopOrder()).isEqualTo(3);
        assertThat(result.direction()).isEqualTo(Direction.OUTBOUND);
    }

    @Test
    @DisplayName("종점 도달 시 stop_order 1, 방향 반전을 반환한다 (OUTBOUND → INBOUND)")
    void nextStop_revertsToOrderOneAndFlipsDirection_whenOutboundReachesEnd() {

        // when
        NextStop result = tracker.nextStop(5, Direction.OUTBOUND, 5);

        // then
        assertThat(result.stopOrder()).isEqualTo(1);
        assertThat(result.direction()).isEqualTo(Direction.INBOUND);
    }

    @Test
    @DisplayName("종점 도달 시 stop_order 1, 방향 반전을 반환한다 (INBOUND → OUTBOUND)")
    void nextStop_revertsToOrderOneAndFlipsDirection_whenInboundReachesEnd() {

        // when
        NextStop result = tracker.nextStop(3, Direction.INBOUND, 3);

        // then
        assertThat(result.stopOrder()).isEqualTo(1);
        assertThat(result.direction()).isEqualTo(Direction.OUTBOUND);
    }

    @Test
    @DisplayName("첫 번째 정류장(1)에서 다음 순서는 2다")
    void nextStop_returnsTwo_whenAtFirstStop() {

        // when
        NextStop result = tracker.nextStop(1, Direction.OUTBOUND, 5);

        // then
        assertThat(result.stopOrder()).isEqualTo(2);
        assertThat(result.direction()).isEqualTo(Direction.OUTBOUND);
    }

    @Test
    @DisplayName("maxStopOrder가 0 이하이면 예외가 발생한다")
    void nextStop_throwsException_whenMaxStopOrderIsZero() {

        // when & then
        assertThatThrownBy(() -> tracker.nextStop(1, Direction.OUTBOUND, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("maxStopOrder가 음수이면 예외가 발생한다")
    void nextStop_throwsException_whenMaxStopOrderIsNegative() {

        // when & then
        assertThatThrownBy(() -> tracker.nextStop(1, Direction.OUTBOUND, -1))
                .isInstanceOf(IllegalStateException.class);
    }
}
