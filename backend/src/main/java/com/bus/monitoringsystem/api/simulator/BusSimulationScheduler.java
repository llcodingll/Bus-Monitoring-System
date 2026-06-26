package com.bus.monitoringsystem.api.simulator;

import com.bus.monitoringsystem.api.bus.model.Bus;
import com.bus.monitoringsystem.api.dispatch.model.BusDispatch;
import com.bus.monitoringsystem.api.dispatch.repository.BusDispatchRepository;
import com.bus.monitoringsystem.api.event.model.Event;
import com.bus.monitoringsystem.api.event.model.EventType;
import com.bus.monitoringsystem.api.event.model.Severity;
import com.bus.monitoringsystem.api.event.repository.EventRepository;
import com.bus.monitoringsystem.api.gps.model.GpsLocation;
import com.bus.monitoringsystem.api.gps.repository.GpsLocationRepository;
import com.bus.monitoringsystem.api.routestop.model.Direction;
import com.bus.monitoringsystem.api.routestop.model.RouteStop;
import com.bus.monitoringsystem.api.routestop.repository.RouteStopRepository;
import com.bus.monitoringsystem.api.simulator.domain.GpsInterpolator;
import com.bus.monitoringsystem.api.simulator.domain.ImpactEventDetector;
import com.bus.monitoringsystem.api.simulator.domain.NextStop;
import com.bus.monitoringsystem.api.simulator.domain.RouteProgressTracker;
import com.bus.monitoringsystem.api.simulator.domain.SpeedBasedEventDetector;
import com.bus.monitoringsystem.api.stop.model.Stop;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class BusSimulationScheduler {

    private static final Duration TICK_INTERVAL = Duration.ofSeconds(7);
    private static final double PROGRESS_PER_TICK = 0.18;
    private static final int IMPACT_CHANCE = 120;
    private static final Set<String> OFFLINE_BUS_NUMBERS = Set.of("서울75바3456", "서울73바7890");

    private final BusDispatchRepository busDispatchRepository;
    private final RouteStopRepository routeStopRepository;
    private final GpsLocationRepository gpsLocationRepository;
    private final EventRepository eventRepository;
    private final GpsInterpolator gpsInterpolator;
    private final RouteProgressTracker routeProgressTracker;
    private final SpeedBasedEventDetector speedEventDetector;
    private final ImpactEventDetector impactDetector;

    private final Map<Long, BusSimState> stateMap = new ConcurrentHashMap<>();
    private final Random random = new Random();

    @Scheduled(fixedDelay = 7000)
    @Transactional
    public void tick() {

        List<BusDispatch> dispatches = busDispatchRepository.findAllActiveWithBusAndStops();

        for (BusDispatch dispatch : dispatches) {
            Bus bus = dispatch.getBus();

            if (OFFLINE_BUS_NUMBERS.contains(bus.getBusNumber())) {
                continue;
            }

            BusSimState state = stateMap.computeIfAbsent(bus.getId(), id -> initState(dispatch));
            if (state == null) {
                continue;
            }

            advanceBus(bus, dispatch, state);
        }
    }

    private BusSimState initState(BusDispatch dispatch) {

        Bus bus = dispatch.getBus();
        Stop currentStop = bus.getCurrentStop();

        if (currentStop == null) {
            return null;
        }

        Optional<Integer> stopOrder = routeStopRepository.findStopOrderByRouteIdAndStopIdAndDirection(
                dispatch.getRoute().getId(), currentStop.getId(), dispatch.getDirection());

        return new BusSimState(
                stopOrder.orElse(1),
                dispatch.getDirection(),
                0.0,
                (double) bus.getCurrentSpeed()
        );
    }

    private void advanceBus(Bus bus, BusDispatch dispatch, BusSimState state) {

        Long routeId = dispatch.getRoute().getId();
        int maxStopOrder = routeStopRepository
                .findMaxStopOrderByRouteIdAndDirection(routeId, state.direction())
                .orElse(1);

        NextStop nextInfo = routeProgressTracker.nextStop(state.currentStopOrder(), state.direction(), maxStopOrder);

        double newProgress = state.progress() + PROGRESS_PER_TICK;

        Stop currentStop;
        Stop nextStop;
        Direction newDirection = state.direction();
        int newCurrentStopOrder = state.currentStopOrder();
        double finalProgress = newProgress;

        if (newProgress >= 1.0) {
            // 다음 정류장 도착 — 방향 전환 포함 처리
            Optional<RouteStop> arrivedRs = routeStopRepository
                    .findWithStopByRouteIdAndDirectionAndStopOrder(routeId, nextInfo.direction(), nextInfo.stopOrder());

            if (arrivedRs.isEmpty()) {
                return;
            }

            currentStop = arrivedRs.get().getStop();
            newCurrentStopOrder = nextInfo.stopOrder();
            newDirection = nextInfo.direction();
            finalProgress = 0.0;

            int newMax = routeStopRepository
                    .findMaxStopOrderByRouteIdAndDirection(routeId, newDirection)
                    .orElse(1);
            NextStop afterNext = routeProgressTracker.nextStop(newCurrentStopOrder, newDirection, newMax);

            Optional<RouteStop> nextRs = routeStopRepository
                    .findWithStopByRouteIdAndDirectionAndStopOrder(routeId, afterNext.direction(), afterNext.stopOrder());

            nextStop = nextRs.map(RouteStop::getStop).orElse(currentStop);

        } else {
            // 정류장 사이 이동
            Optional<RouteStop> currentRs = routeStopRepository
                    .findWithStopByRouteIdAndDirectionAndStopOrder(routeId, state.direction(), state.currentStopOrder());
            Optional<RouteStop> nextRs = routeStopRepository
                    .findWithStopByRouteIdAndDirectionAndStopOrder(routeId, nextInfo.direction(), nextInfo.stopOrder());

            if (currentRs.isEmpty() || nextRs.isEmpty()) {
                return;
            }

            currentStop = currentRs.get().getStop();
            nextStop = nextRs.get().getStop();
        }

        double[] interpolated = interpolatePosition(currentStop, nextStop, finalProgress, newProgress >= 1.0);
        int newSpeed = simulateSpeed(newProgress >= 1.0);

        detectAndSaveEvents(bus, dispatch, state.prevSpeedKmh(), newSpeed, interpolated);
        saveGpsLocation(bus, dispatch, interpolated, newSpeed);
        bus.updateLocation(
                BigDecimal.valueOf(interpolated[0]),
                BigDecimal.valueOf(interpolated[1]),
                newSpeed, currentStop, nextStop,
                LocalDateTime.now()
        );

        stateMap.put(bus.getId(), new BusSimState(newCurrentStopOrder, newDirection, finalProgress, (double) newSpeed));
    }

    private double[] interpolatePosition(Stop from, Stop to, double progress, boolean arrived) {

        if (arrived) {
            return new double[]{to.getLatitude().doubleValue(), to.getLongitude().doubleValue()};
        }

        return gpsInterpolator.interpolate(
                from.getLatitude().doubleValue(), from.getLongitude().doubleValue(),
                to.getLatitude().doubleValue(), to.getLongitude().doubleValue(),
                progress
        );
    }

    private int simulateSpeed(boolean arrivedAtStop) {

        if (arrivedAtStop) {
            return random.nextInt(10);
        }
        return 20 + random.nextInt(41);
    }

    private void detectAndSaveEvents(Bus bus, BusDispatch dispatch,
                                     Double prevSpeed, int currentSpeed,
                                     double[] position) {

        Optional<EventType> speedEvent = speedEventDetector.detect(prevSpeed, currentSpeed, TICK_INTERVAL);
        speedEvent.ifPresent(type -> saveEvent(bus, dispatch, type, determineSeverity(type), position));

        if (random.nextInt(IMPACT_CHANCE) == 0) {
            double impactG = 0.5 + random.nextDouble() * 0.5;
            if (impactDetector.isImpact(impactG)) {
                saveEvent(bus, dispatch, EventType.IMPACT, Severity.HIGH, position);
            }
        }
    }

    private Severity determineSeverity(EventType type) {

        return switch (type) {
            case SUDDEN_BRAKE, SUDDEN_START -> Severity.HIGH;
            case SUDDEN_ACCELERATION, SUDDEN_DECELERATION -> Severity.MEDIUM;
            default -> Severity.LOW;
        };
    }

    private void saveEvent(Bus bus, BusDispatch dispatch, EventType type,
                           Severity severity, double[] position) {

        eventRepository.save(Event.builder()
                .bus(bus)
                .dispatch(dispatch)
                .route(dispatch.getRoute())
                .eventType(type)
                .severity(severity)
                .latitude(BigDecimal.valueOf(position[0]))
                .longitude(BigDecimal.valueOf(position[1]))
                .occurredAt(LocalDateTime.now())
                .build());
    }

    private void saveGpsLocation(Bus bus, BusDispatch dispatch, double[] position, int speed) {

        gpsLocationRepository.save(GpsLocation.builder()
                .bus(bus)
                .dispatch(dispatch)
                .latitude(BigDecimal.valueOf(position[0]))
                .longitude(BigDecimal.valueOf(position[1]))
                .speed(speed)
                .recordedAt(LocalDateTime.now())
                .build());
    }
}
