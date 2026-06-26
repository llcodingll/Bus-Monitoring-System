package com.bus.monitoringsystem.api.seed.dto.result;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SeedResult {

    private final int busCount;
    private final int routeCount;
    private final int stopCount;
    private final int eventCount;
}
