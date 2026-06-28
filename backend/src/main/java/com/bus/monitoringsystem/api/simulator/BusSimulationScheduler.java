package com.bus.monitoringsystem.api.simulator;

import com.bus.monitoringsystem.api.simulator.cache.ActiveDispatchCache;
import com.bus.monitoringsystem.api.simulator.cache.ActiveDispatchDto;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
@RequiredArgsConstructor
public class BusSimulationScheduler {

    private static final Set<String> OFFLINE_BUS_NUMBERS = Set.of("서울75바3456", "서울73바7890");

    private final ActiveDispatchCache activeDispatchCache;
    private final BusTickProcessor busTickProcessor;

    private final Map<Long, BusSimState> stateMap = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(
            Math.min(Runtime.getRuntime().availableProcessors(), 10)
    );

    @Scheduled(fixedDelay = 7000)
    public void tick() {

        List<Callable<Void>> tasks = new ArrayList<>();

        for (ActiveDispatchDto dto : activeDispatchCache.getAll()) {
            if (OFFLINE_BUS_NUMBERS.contains(dto.busNumber())) {
                continue;
            }

            BusSimState state = stateMap.get(dto.busId());

            tasks.add(() -> {
                try {
                    BusSimState newState = busTickProcessor.process(dto, state);
                    if (newState != null) {
                        stateMap.put(dto.busId(), newState);
                    }
                } catch (Exception e) {
                    log.warn("tick 실패 — busId={}: {}", dto.busId(), e.getMessage());
                }
                return null;
            });
        }

        try {
            executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("tick interrupted");
        }
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdown();
    }

    public void clearState() {
        stateMap.clear();
    }
}
