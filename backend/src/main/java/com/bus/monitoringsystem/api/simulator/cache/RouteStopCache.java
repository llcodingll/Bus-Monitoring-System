package com.bus.monitoringsystem.api.simulator.cache;

import com.bus.monitoringsystem.api.routestop.model.Direction;
import com.bus.monitoringsystem.api.routestop.model.RouteStop;
import com.bus.monitoringsystem.api.routestop.repository.RouteStopRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RouteStopCache {

    private final RouteStopRepository routeStopRepository;

    private Map<Long, Map<Direction, Map<Integer, RouteStop>>> byStopOrder = Map.of();
    private Map<Long, Map<Direction, Integer>> maxStopOrderMap = Map.of();
    private Map<Long, Map<Direction, Map<Long, Integer>>> stopOrderByStopId = Map.of();

    @PostConstruct
    public void load() {

        List<RouteStop> all = routeStopRepository.findAllWithStop();

        Map<Long, Map<Direction, Map<Integer, RouteStop>>> byOrder = new HashMap<>();
        Map<Long, Map<Direction, Integer>> maxOrder = new HashMap<>();
        Map<Long, Map<Direction, Map<Long, Integer>>> byStopId = new HashMap<>();

        for (RouteStop rs : all) {
            Long routeId = rs.getRoute().getId();
            Direction dir = rs.getDirection();
            int order = rs.getStopOrder();
            Long stopId = rs.getStop().getId();

            byOrder.computeIfAbsent(routeId, k -> new HashMap<>())
                    .computeIfAbsent(dir, k -> new HashMap<>())
                    .put(order, rs);

            maxOrder.computeIfAbsent(routeId, k -> new HashMap<>())
                    .merge(dir, order, Math::max);

            byStopId.computeIfAbsent(routeId, k -> new HashMap<>())
                    .computeIfAbsent(dir, k -> new HashMap<>())
                    .put(stopId, order);
        }

        this.byStopOrder = byOrder;
        this.maxStopOrderMap = maxOrder;
        this.stopOrderByStopId = byStopId;
    }

    public Optional<RouteStop> findByRouteAndDirectionAndStopOrder(Long routeId, Direction direction, int stopOrder) {

        return Optional.ofNullable(
                byStopOrder.getOrDefault(routeId, Map.of())
                        .getOrDefault(direction, Map.of())
                        .get(stopOrder)
        );
    }

    public Optional<Integer> findMaxStopOrder(Long routeId, Direction direction) {

        return Optional.ofNullable(
                maxStopOrderMap.getOrDefault(routeId, Map.of())
                        .get(direction)
        );
    }

    public Optional<Integer> findStopOrderByStopId(Long routeId, Direction direction, Long stopId) {

        return Optional.ofNullable(
                stopOrderByStopId.getOrDefault(routeId, Map.of())
                        .getOrDefault(direction, Map.of())
                        .get(stopId)
        );
    }
}
