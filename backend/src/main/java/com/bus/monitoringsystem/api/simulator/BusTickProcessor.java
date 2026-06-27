package com.bus.monitoringsystem.api.simulator;

import com.bus.monitoringsystem.api.bus.model.Bus;
import com.bus.monitoringsystem.api.bus.repository.BusRepository;
import com.bus.monitoringsystem.api.dispatch.model.BusDispatch;
import com.bus.monitoringsystem.api.dispatch.repository.BusDispatchRepository;
import com.bus.monitoringsystem.api.event.model.Event;
import com.bus.monitoringsystem.api.event.model.EventType;
import com.bus.monitoringsystem.api.event.model.Severity;
import com.bus.monitoringsystem.api.event.repository.EventRepository;
import com.bus.monitoringsystem.api.gps.model.GpsLocation;
import com.bus.monitoringsystem.api.gps.repository.GpsLocationRepository;
import com.bus.monitoringsystem.api.route.model.Route;
import com.bus.monitoringsystem.api.route.repository.RouteRepository;
import com.bus.monitoringsystem.api.routestop.model.Direction;
import com.bus.monitoringsystem.api.routestop.model.RouteStop;
import com.bus.monitoringsystem.api.simulator.cache.ActiveDispatchDto;
import com.bus.monitoringsystem.api.simulator.cache.RouteStopCache;
import com.bus.monitoringsystem.api.simulator.domain.GpsInterpolator;
import com.bus.monitoringsystem.api.simulator.domain.ImpactEventDetector;
import com.bus.monitoringsystem.api.simulator.domain.NextStop;
import com.bus.monitoringsystem.api.simulator.domain.RouteProgressTracker;
import com.bus.monitoringsystem.api.simulator.domain.SpeedBasedEventDetector;
import com.bus.monitoringsystem.api.stop.model.Stop;
import com.bus.monitoringsystem.api.stop.repository.StopRepository;
import com.bus.monitoringsystem.common.BaseException;
import com.bus.monitoringsystem.common.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class BusTickProcessor {

    private static final Duration TICK_INTERVAL = Duration.ofSeconds(7);
    private static final double PROGRESS_PER_TICK = 0.18;
    private static final int IMPACT_CHANCE = 120;
    private static final Random RANDOM = new Random();

    private final BusRepository busRepository;
    private final BusDispatchRepository busDispatchRepository;
    private final StopRepository stopRepository;
    private final RouteRepository routeRepository;
    private final GpsLocationRepository gpsLocationRepository;
    private final EventRepository eventRepository;
    private final RouteStopCache routeStopCache;
    private final GpsInterpolator gpsInterpolator;
    private final RouteProgressTracker routeProgressTracker;
    private final SpeedBasedEventDetector speedEventDetector;
    private final ImpactEventDetector impactDetector;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BusSimState process(ActiveDispatchDto dto, BusSimState state) {

        Bus bus = busRepository.findByIdWithStops(dto.busId())
                .orElseThrow(() -> new BaseException(BaseResponseStatus.BUS_NOT_FOUND));

        if (state == null) {
            state = initState(bus, dto);
            if (state == null) {
                return null;
            }
        }

        BusDispatch dispatch = busDispatchRepository.getReferenceById(dto.dispatchId());
        Route route = routeRepository.getReferenceById(dto.routeId());

        return advanceBus(bus, dispatch, route, dto.routeId(), state);
    }

    private BusSimState initState(Bus bus, ActiveDispatchDto dto) {

        Stop currentStop = bus.getCurrentStop();
        if (currentStop == null) {
            return null;
        }

        Optional<Integer> stopOrder = routeStopCache.findStopOrderByStopId(
                dto.routeId(), dto.direction(), currentStop.getId());

        return new BusSimState(stopOrder.orElse(1), dto.direction(), 0.0, (double) bus.getCurrentSpeed());
    }

    private BusSimState advanceBus(Bus bus, BusDispatch dispatch, Route route, Long routeId, BusSimState state) {

        int maxStopOrder = routeStopCache.findMaxStopOrder(routeId, state.direction()).orElse(1);
        NextStop nextInfo = routeProgressTracker.nextStop(state.currentStopOrder(), state.direction(), maxStopOrder);

        double newProgress = state.progress() + PROGRESS_PER_TICK;

        Stop currentStop;
        Stop nextStop;
        Direction newDirection = state.direction();
        int newCurrentStopOrder = state.currentStopOrder();
        double finalProgress = newProgress;

        if (newProgress >= 1.0) {
            Optional<RouteStop> arrivedRs = routeStopCache.findByRouteAndDirectionAndStopOrder(
                    routeId, nextInfo.direction(), nextInfo.stopOrder());

            if (arrivedRs.isEmpty()) {
                return state;
            }

            currentStop = arrivedRs.get().getStop();
            newCurrentStopOrder = nextInfo.stopOrder();
            newDirection = nextInfo.direction();
            finalProgress = 0.0;

            int newMax = routeStopCache.findMaxStopOrder(routeId, newDirection).orElse(1);
            NextStop afterNext = routeProgressTracker.nextStop(newCurrentStopOrder, newDirection, newMax);

            Optional<RouteStop> nextRs = routeStopCache.findByRouteAndDirectionAndStopOrder(
                    routeId, afterNext.direction(), afterNext.stopOrder());

            nextStop = nextRs.map(RouteStop::getStop).orElse(currentStop);

        } else {
            Optional<RouteStop> currentRs = routeStopCache.findByRouteAndDirectionAndStopOrder(
                    routeId, state.direction(), state.currentStopOrder());
            Optional<RouteStop> nextRs = routeStopCache.findByRouteAndDirectionAndStopOrder(
                    routeId, nextInfo.direction(), nextInfo.stopOrder());

            if (currentRs.isEmpty() || nextRs.isEmpty()) {
                return state;
            }

            currentStop = currentRs.get().getStop();
            nextStop = nextRs.get().getStop();
        }

        double[] interpolated = interpolatePosition(currentStop, nextStop, finalProgress, newProgress >= 1.0);
        int newSpeed = simulateSpeed(newProgress >= 1.0);

        detectAndSaveEvents(bus, dispatch, route, state.prevSpeedKmh(), newSpeed, interpolated);
        saveGpsLocation(bus, dispatch, interpolated, newSpeed);

        bus.updateLocation(
                BigDecimal.valueOf(interpolated[0]),
                BigDecimal.valueOf(interpolated[1]),
                newSpeed,
                stopRepository.getReferenceById(currentStop.getId()),
                stopRepository.getReferenceById(nextStop.getId()),
                LocalDateTime.now()
        );

        return new BusSimState(newCurrentStopOrder, newDirection, finalProgress, (double) newSpeed);
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
            return RANDOM.nextInt(10);
        }
        return 20 + RANDOM.nextInt(41);
    }

    private void detectAndSaveEvents(Bus bus, BusDispatch dispatch, Route route,
                                     Double prevSpeed, int currentSpeed, double[] position) {

        Optional<EventType> speedEvent = speedEventDetector.detect(prevSpeed, currentSpeed, TICK_INTERVAL);
        speedEvent.ifPresent(type -> saveEvent(bus, dispatch, route, type, determineSeverity(type), position));

        if (RANDOM.nextInt(IMPACT_CHANCE) == 0) {
            double impactG = 0.5 + RANDOM.nextDouble() * 0.5;
            if (impactDetector.isImpact(impactG)) {
                saveEvent(bus, dispatch, route, EventType.IMPACT, Severity.HIGH, position);
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

    private void saveEvent(Bus bus, BusDispatch dispatch, Route route,
                           EventType type, Severity severity, double[] position) {

        eventRepository.save(Event.builder()
                .bus(bus)
                .dispatch(dispatch)
                .route(route)
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
