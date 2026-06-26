package com.bus.monitoringsystem.api.simulator.domain;

import com.bus.monitoringsystem.api.event.model.EventType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SpeedBasedEventDetector")
class SpeedBasedEventDetectorTest {

    private final SpeedBasedEventDetector detector = new SpeedBasedEventDetector();

    private static final Duration ONE_SECOND = Duration.ofSeconds(1);

    @Test
    @DisplayName("prevSpeedKmh가 null이면 빈 Optional을 반환한다")
    void detect_returnsEmpty_whenPrevSpeedIsNull() {

        // when
        Optional<EventType> result = detector.detect(null, 50.0, ONE_SECOND);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("prevSpeedKmh가 음수이면 빈 Optional을 반환한다")
    void detect_returnsEmpty_whenPrevSpeedIsNegative() {

        // when
        Optional<EventType> result = detector.detect(-1.0, 50.0, ONE_SECOND);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("interval이 0이면 빈 Optional을 반환한다")
    void detect_returnsEmpty_whenIntervalIsZero() {

        // when
        Optional<EventType> result = detector.detect(50.0, 80.0, Duration.ZERO);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("interval이 음수이면 빈 Optional을 반환한다")
    void detect_returnsEmpty_whenIntervalIsNegative() {

        // when
        Optional<EventType> result = detector.detect(50.0, 80.0, Duration.ofSeconds(-1));

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("가속도가 0.3g 미만이면 빈 Optional을 반환한다")
    void detect_returnsEmpty_whenAccelerationBelowThreshold() {

        // when — 50→52km/h in 1s = ~0.057g < 0.3g
        Optional<EventType> result = detector.detect(50.0, 52.0, ONE_SECOND);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("이전 속도 5km/h 이하에서 급가속이면 급출발을 반환한다")
    void detect_returnsSuddenStart_whenAcceleratingFromStop() {

        // when — 0→40km/h in 1s = ~1.13g > 0.3g, prev <= 5.0
        Optional<EventType> result = detector.detect(0.0, 40.0, ONE_SECOND);

        // then
        assertThat(result).contains(EventType.SUDDEN_START);
    }

    @Test
    @DisplayName("이전 속도 정확히 5km/h에서 급가속이면 급출발을 반환한다")
    void detect_returnsSuddenStart_whenPrevSpeedExactlyAtStopThreshold() {

        // when — 5→45km/h in 1s, prev == 5.0 (경계값)
        Optional<EventType> result = detector.detect(5.0, 45.0, ONE_SECOND);

        // then
        assertThat(result).contains(EventType.SUDDEN_START);
    }

    @Test
    @DisplayName("이전 속도 5km/h 초과에서 급가속이면 급가속을 반환한다")
    void detect_returnsSuddenAcceleration_whenAcceleratingFromMoving() {

        // when — 40→80km/h in 1s = ~1.13g > 0.3g, prev > 5.0
        Optional<EventType> result = detector.detect(40.0, 80.0, ONE_SECOND);

        // then
        assertThat(result).contains(EventType.SUDDEN_ACCELERATION);
    }

    @Test
    @DisplayName("현재 속도 5km/h 이하로 급감속이면 급정거를 반환한다")
    void detect_returnsSuddenBrake_whenDeceleratingToStop() {

        // when — 80→0km/h in 1s, current <= 5.0
        Optional<EventType> result = detector.detect(80.0, 0.0, ONE_SECOND);

        // then
        assertThat(result).contains(EventType.SUDDEN_BRAKE);
    }

    @Test
    @DisplayName("현재 속도 정확히 5km/h로 급감속이면 급정거를 반환한다")
    void detect_returnsSuddenBrake_whenCurrentSpeedExactlyAtStopThreshold() {

        // when — 80→5km/h in 1s, current == 5.0 (경계값)
        Optional<EventType> result = detector.detect(80.0, 5.0, ONE_SECOND);

        // then
        assertThat(result).contains(EventType.SUDDEN_BRAKE);
    }

    @Test
    @DisplayName("현재 속도 5km/h 초과로 급감속이면 급감속을 반환한다")
    void detect_returnsSuddenDeceleration_whenDeceleratingWhileMoving() {

        // when — 80→40km/h in 1s = ~1.13g > 0.3g, current > 5.0
        Optional<EventType> result = detector.detect(80.0, 40.0, ONE_SECOND);

        // then
        assertThat(result).contains(EventType.SUDDEN_DECELERATION);
    }

    @Test
    @DisplayName("가속도가 정확히 0.3g 미만 경계값이면 빈 Optional을 반환한다")
    void detect_returnsEmpty_whenAccelerationJustBelowThreshold() {

        // 0→10km/h in 1s = 2.778m/s / 9.81 = ~0.283g < 0.3g
        Optional<EventType> result = detector.detect(0.0, 10.0, ONE_SECOND);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("가속도가 정확히 0.3g 이상 경계값이면 이벤트를 반환한다")
    void detect_returnsEvent_whenAccelerationAtThreshold() {

        // 0→11km/h in 1s = 3.056m/s / 9.81 = ~0.311g >= 0.3g
        Optional<EventType> result = detector.detect(0.0, 11.0, ONE_SECOND);

        // then
        assertThat(result).isPresent();
    }
}
