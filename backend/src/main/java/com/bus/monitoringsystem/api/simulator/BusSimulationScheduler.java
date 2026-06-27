package com.bus.monitoringsystem.api.simulator;

import com.bus.monitoringsystem.api.simulator.cache.ActiveDispatchCache;
import com.bus.monitoringsystem.api.simulator.cache.ActiveDispatchDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class BusSimulationScheduler {

    private static final Set<String> OFFLINE_BUS_NUMBERS = Set.of("서울75바3456", "서울73바7890");

    private final ActiveDispatchCache activeDispatchCache;
    private final BusTickProcessor busTickProcessor;

    private final Map<Long, BusSimState> stateMap = new ConcurrentHashMap<>();

    @Scheduled(fixedDelay = 7000)
    public void tick() {

        for (ActiveDispatchDto dto : activeDispatchCache.getAll()) {
            if (OFFLINE_BUS_NUMBERS.contains(dto.busNumber())) {
                continue;
            }

            BusSimState state = stateMap.get(dto.busId());

            try {
                BusSimState newState = busTickProcessor.process(dto, state);
                if (newState != null) {
                    stateMap.put(dto.busId(), newState);
                }
            } catch (Exception e) {
                log.warn("tick 실패 — busId={}: {}", dto.busId(), e.getMessage());
            }
        }
    }

    public void clearState() {

        stateMap.clear();
    }
}
