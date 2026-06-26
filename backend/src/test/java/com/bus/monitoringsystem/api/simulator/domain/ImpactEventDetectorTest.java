package com.bus.monitoringsystem.api.simulator.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ImpactEventDetector")
class ImpactEventDetectorTest {

    private final ImpactEventDetector detector = new ImpactEventDetector();

    @Test
    @DisplayName("충격강도가 0.5g 미만이면 false를 반환한다")
    void isImpact_returnsFalse_whenMagnitudeBelowThreshold() {

        // when
        boolean result = detector.isImpact(0.4);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("충격강도가 정확히 0.5g이면 true를 반환한다")
    void isImpact_returnsTrue_whenMagnitudeExactlyAtThreshold() {

        // when
        boolean result = detector.isImpact(0.5);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("충격강도가 0.5g 초과이면 true를 반환한다")
    void isImpact_returnsTrue_whenMagnitudeAboveThreshold() {

        // when
        boolean result = detector.isImpact(1.0);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("음수 충격강도의 절댓값이 0.5g 이상이면 true를 반환한다")
    void isImpact_returnsTrue_whenNegativeMagnitudeAbsValueMeetsThreshold() {

        // when — Math.abs(-0.7) = 0.7 >= 0.5
        boolean result = detector.isImpact(-0.7);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("음수 충격강도의 절댓값이 0.5g 미만이면 false를 반환한다")
    void isImpact_returnsFalse_whenNegativeMagnitudeAbsValueBelowThreshold() {

        // when — Math.abs(-0.3) = 0.3 < 0.5
        boolean result = detector.isImpact(-0.3);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("충격강도가 0이면 false를 반환한다")
    void isImpact_returnsFalse_whenMagnitudeIsZero() {

        // when
        boolean result = detector.isImpact(0.0);

        // then
        assertThat(result).isFalse();
    }
}
