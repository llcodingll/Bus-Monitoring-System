package com.bus.monitoringsystem.api.seed.dto.response;

import com.bus.monitoringsystem.api.seed.dto.result.SeedResult;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SeedResponse {

    private final String message;
    private final int busCount;
    private final int routeCount;
    private final int stopCount;
    private final int eventCount;

    public static SeedResponse from(SeedResult result) {

        return SeedResponse.builder()
                .message("목업 데이터가 성공적으로 생성되었습니다.")
                .busCount(result.getBusCount())
                .routeCount(result.getRouteCount())
                .stopCount(result.getStopCount())
                .eventCount(result.getEventCount())
                .build();
    }
}
