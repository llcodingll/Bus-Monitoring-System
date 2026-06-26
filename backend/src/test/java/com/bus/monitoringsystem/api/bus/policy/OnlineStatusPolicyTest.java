package com.bus.monitoringsystem.api.bus.policy;

import com.bus.monitoringsystem.api.bus.model.BusStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OnlineStatusPolicy")
class OnlineStatusPolicyTest {

    private final OnlineStatusPolicy policy = new OnlineStatusPolicy();

    @Test
    @DisplayName("마지막 통신이 방금(0초 경과)이면 ONLINE이다")
    void resolve_returnsOnline_whenElapsedIsZero() {

        // given
        Instant now = Instant.parse("2026-06-22T10:00:00Z");
        Instant last = Instant.parse("2026-06-22T10:00:00Z");

        // when
        BusStatus result = policy.resolve(last, now);

        // then
        assertThat(result).isEqualTo(BusStatus.ONLINE);
    }

    @Test
    @DisplayName("마지막 통신이 4분 59초 전이면 ONLINE이다")
    void resolve_returnsOnline_whenElapsedIs4min59sec() {

        // given
        Instant now = Instant.parse("2026-06-22T10:05:00Z");
        Instant last = Instant.parse("2026-06-22T10:00:01Z");

        // when
        BusStatus result = policy.resolve(last, now);

        // then
        assertThat(result).isEqualTo(BusStatus.ONLINE);
    }

    @Test
    @DisplayName("마지막 통신이 정확히 5분 전이면 ONLINE이다 (경계 포함)")
    void resolve_returnsOnline_whenElapsedIsExactly5min() {

        // given
        Instant now = Instant.parse("2026-06-22T10:05:00Z");
        Instant last = Instant.parse("2026-06-22T10:00:00Z");

        // when
        BusStatus result = policy.resolve(last, now);

        // then
        assertThat(result).isEqualTo(BusStatus.ONLINE);
    }

    @Test
    @DisplayName("마지막 통신이 5분을 1ms라도 초과하면 OFFLINE이다")
    void resolve_returnsOffline_whenElapsedIs5minAnd1ms() {

        // given
        Instant now = Instant.parse("2026-06-22T10:05:00.001Z");
        Instant last = Instant.parse("2026-06-22T10:00:00Z");

        // when
        BusStatus result = policy.resolve(last, now);

        // then
        assertThat(result).isEqualTo(BusStatus.OFFLINE);
    }

    @Test
    @DisplayName("마지막 통신이 5분 1초 전이면 OFFLINE이다")
    void resolve_returnsOffline_whenElapsedIs5min1sec() {

        // given
        Instant now = Instant.parse("2026-06-22T10:05:01Z");
        Instant last = Instant.parse("2026-06-22T10:00:00Z");

        // when
        BusStatus result = policy.resolve(last, now);

        // then
        assertThat(result).isEqualTo(BusStatus.OFFLINE);
    }

    @Test
    @DisplayName("마지막 통신이 10분 전이면 OFFLINE이다")
    void resolve_returnsOffline_whenElapsedIs10min() {

        // given
        Instant now = Instant.parse("2026-06-22T10:10:00Z");
        Instant last = Instant.parse("2026-06-22T10:00:00Z");

        // when
        BusStatus result = policy.resolve(last, now);

        // then
        assertThat(result).isEqualTo(BusStatus.OFFLINE);
    }

    @Test
    @DisplayName("마지막 통신이 1일 전이어도 OFFLINE이다 (스케일 무관)")
    void resolve_returnsOffline_whenElapsedIs1day() {

        // given
        Instant now = Instant.parse("2026-06-23T10:00:00Z");
        Instant last = Instant.parse("2026-06-22T10:00:00Z");

        // when
        BusStatus result = policy.resolve(last, now);

        // then
        assertThat(result).isEqualTo(BusStatus.OFFLINE);
    }

    @Test
    @DisplayName("한 번도 통신하지 않은 버스(null)는 OFFLINE이다")
    void resolve_returnsOffline_whenLastCommunicatedAtIsNull() {

        // given
        Instant now = Instant.now();

        // when
        BusStatus result = policy.resolve(null, now);

        // then
        assertThat(result).isEqualTo(BusStatus.OFFLINE);
    }

    @Test
    @DisplayName("시계 역전(now가 last보다 과거)이어도 예외 없이 ONLINE으로 처리한다")
    void resolve_returnsOnline_whenClockIsSkewed() {

        // given
        Instant last = Instant.parse("2026-06-22T10:05:00Z");
        Instant now = Instant.parse("2026-06-22T10:00:00Z");

        // when
        BusStatus result = policy.resolve(last, now);

        // then
        assertThat(result).isEqualTo(BusStatus.ONLINE);
    }

    @Test
    @DisplayName("lastCommunicatedAt과 now가 동일 시각이면 ONLINE이다")
    void resolve_returnsOnline_whenLastEqualsNow() {

        // given
        Instant now = Instant.parse("2026-06-22T10:00:00Z");
        Instant last = Instant.parse("2026-06-22T10:00:00Z");

        // when
        BusStatus result = policy.resolve(last, now);

        // then
        assertThat(result).isEqualTo(BusStatus.ONLINE);
    }
}
