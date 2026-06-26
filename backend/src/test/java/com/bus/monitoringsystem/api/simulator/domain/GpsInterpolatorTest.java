package com.bus.monitoringsystem.api.simulator.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@DisplayName("GpsInterpolator")
class GpsInterpolatorTest {

    private final GpsInterpolator interpolator = new GpsInterpolator();

    private static final double FROM_LAT = 37.5000;
    private static final double FROM_LNG = 127.0000;
    private static final double TO_LAT = 37.6000;
    private static final double TO_LNG = 127.1000;

    @Test
    @DisplayName("progress가 0이면 from 좌표를 그대로 반환한다")
    void interpolate_returnsFrom_whenProgressIsZero() {

        // when
        double[] result = interpolator.interpolate(FROM_LAT, FROM_LNG, TO_LAT, TO_LNG, 0.0);

        // then
        assertThat(result[0]).isCloseTo(FROM_LAT, within(0.0001));
        assertThat(result[1]).isCloseTo(FROM_LNG, within(0.0001));
    }

    @Test
    @DisplayName("progress가 1이면 to 좌표를 그대로 반환한다")
    void interpolate_returnsTo_whenProgressIsOne() {

        // when
        double[] result = interpolator.interpolate(FROM_LAT, FROM_LNG, TO_LAT, TO_LNG, 1.0);

        // then
        assertThat(result[0]).isCloseTo(TO_LAT, within(0.0001));
        assertThat(result[1]).isCloseTo(TO_LNG, within(0.0001));
    }

    @Test
    @DisplayName("progress가 0.5이면 두 좌표의 중간값을 반환한다")
    void interpolate_returnsMidpoint_whenProgressIsHalf() {

        // when
        double[] result = interpolator.interpolate(FROM_LAT, FROM_LNG, TO_LAT, TO_LNG, 0.5);

        // then
        assertThat(result[0]).isCloseTo(37.5500, within(0.0001));
        assertThat(result[1]).isCloseTo(127.0500, within(0.0001));
    }

    @Test
    @DisplayName("progress가 1을 초과하면 to 좌표로 clamp한다")
    void interpolate_clampsToTarget_whenProgressExceedsOne() {

        // when
        double[] result = interpolator.interpolate(FROM_LAT, FROM_LNG, TO_LAT, TO_LNG, 1.3);

        // then
        assertThat(result[0]).isCloseTo(TO_LAT, within(0.0001));
        assertThat(result[1]).isCloseTo(TO_LNG, within(0.0001));
    }

    @Test
    @DisplayName("progress가 음수이면 from 좌표로 clamp한다")
    void interpolate_clampsToFrom_whenProgressIsNegative() {

        // when
        double[] result = interpolator.interpolate(FROM_LAT, FROM_LNG, TO_LAT, TO_LNG, -0.2);

        // then
        assertThat(result[0]).isCloseTo(FROM_LAT, within(0.0001));
        assertThat(result[1]).isCloseTo(FROM_LNG, within(0.0001));
    }

    @Test
    @DisplayName("from과 to가 같은 좌표이면 progress와 무관하게 같은 값을 반환한다")
    void interpolate_returnsSame_whenFromEqualsTo() {

        // when
        double[] result = interpolator.interpolate(FROM_LAT, FROM_LNG, FROM_LAT, FROM_LNG, 0.7);

        // then
        assertThat(result[0]).isCloseTo(FROM_LAT, within(0.0001));
        assertThat(result[1]).isCloseTo(FROM_LNG, within(0.0001));
    }
}
