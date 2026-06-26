package com.bus.monitoringsystem.api.simulator;

import com.bus.monitoringsystem.api.bus.model.Bus;
import com.bus.monitoringsystem.api.dispatch.model.BusDispatch;
import com.bus.monitoringsystem.api.dispatch.repository.BusDispatchRepository;
import com.bus.monitoringsystem.api.event.repository.EventRepository;
import com.bus.monitoringsystem.api.gps.model.GpsLocation;
import com.bus.monitoringsystem.api.gps.repository.GpsLocationRepository;
import com.bus.monitoringsystem.api.route.model.Route;
import com.bus.monitoringsystem.api.routestop.model.Direction;
import com.bus.monitoringsystem.api.routestop.model.RouteStop;
import com.bus.monitoringsystem.api.routestop.repository.RouteStopRepository;
import com.bus.monitoringsystem.api.simulator.domain.GpsInterpolator;
import com.bus.monitoringsystem.api.simulator.domain.ImpactEventDetector;
import com.bus.monitoringsystem.api.simulator.domain.RouteProgressTracker;
import com.bus.monitoringsystem.api.simulator.domain.SpeedBasedEventDetector;
import com.bus.monitoringsystem.api.stop.model.Stop;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("BusSimulationScheduler")
class BusSimulationSmokeTest {

    @Mock private BusDispatchRepository busDispatchRepository;
    @Mock private RouteStopRepository routeStopRepository;
    @Mock private GpsLocationRepository gpsLocationRepository;
    @Mock private EventRepository eventRepository;

    private BusSimulationScheduler scheduler;

    private Bus bus;
    private BusDispatch dispatch;
    private RouteStop routeStop1;
    private RouteStop routeStop2;

    @BeforeEach
    void setUp() {

        scheduler = new BusSimulationScheduler(
                busDispatchRepository, routeStopRepository, gpsLocationRepository, eventRepository,
                new GpsInterpolator(), new RouteProgressTracker(),
                new SpeedBasedEventDetector(), new ImpactEventDetector()
        );

        Stop stop1 = Stop.builder()
                .stopName("정류장1")
                .latitude(new BigDecimal("37.5600"))
                .longitude(new BigDecimal("126.9700"))
                .build();
        ReflectionTestUtils.setField(stop1, "id", 1L);

        Stop stop2 = Stop.builder()
                .stopName("정류장2")
                .latitude(new BigDecimal("37.5700"))
                .longitude(new BigDecimal("126.9800"))
                .build();
        ReflectionTestUtils.setField(stop2, "id", 2L);

        Route route = Route.builder()
                .routeNumber("370")
                .routeName("은평구청행")
                .build();
        ReflectionTestUtils.setField(route, "id", 1L);

        bus = Bus.builder()
                .busNumber("서울71가1234")
                .currentSpeed(30)
                .currentLatitude(new BigDecimal("37.5600"))
                .currentLongitude(new BigDecimal("126.9700"))
                .currentStop(stop1)
                .build();
        ReflectionTestUtils.setField(bus, "id", 1L);

        dispatch = BusDispatch.builder()
                .bus(bus)
                .route(route)
                .direction(Direction.OUTBOUND)
                .dispatchedDate(LocalDate.now())
                .operationStartedAt(LocalDateTime.now())
                .build();

        routeStop1 = RouteStop.builder()
                .route(route)
                .stop(stop1)
                .stopOrder(1)
                .direction(Direction.OUTBOUND)
                .build();

        routeStop2 = RouteStop.builder()
                .route(route)
                .stop(stop2)
                .stopOrder(2)
                .direction(Direction.OUTBOUND)
                .build();
    }

    @Test
    @DisplayName("1 tick 실행 후 gps_locations에 현재 위치가 저장된다")
    void tick_savesGpsLocation_afterOneTick() {

        // given
        given(busDispatchRepository.findAllActiveWithBusAndStops()).willReturn(List.of(dispatch));
        given(routeStopRepository.findStopOrderByRouteIdAndStopIdAndDirection(1L, 1L, Direction.OUTBOUND))
                .willReturn(Optional.of(1));
        given(routeStopRepository.findMaxStopOrderByRouteIdAndDirection(1L, Direction.OUTBOUND))
                .willReturn(Optional.of(5));
        given(routeStopRepository.findWithStopByRouteIdAndDirectionAndStopOrder(1L, Direction.OUTBOUND, 1))
                .willReturn(Optional.of(routeStop1));
        given(routeStopRepository.findWithStopByRouteIdAndDirectionAndStopOrder(1L, Direction.OUTBOUND, 2))
                .willReturn(Optional.of(routeStop2));

        // when
        scheduler.tick();

        // then
        then(gpsLocationRepository).should().save(any(GpsLocation.class));
    }

    @Test
    @DisplayName("OFFLINE 지정 버스는 tick에서 건너뛰어 GPS가 저장되지 않는다")
    void tick_skipsOfflineBus_withoutSavingGps() {

        // given
        ReflectionTestUtils.setField(bus, "busNumber", "서울75바3456");
        given(busDispatchRepository.findAllActiveWithBusAndStops()).willReturn(List.of(dispatch));

        // when
        scheduler.tick();

        // then
        then(gpsLocationRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("2번째 tick에서도 gps_locations에 위치가 저장된다")
    void tick_savesGpsLocation_onSecondTick() {

        // given
        given(busDispatchRepository.findAllActiveWithBusAndStops()).willReturn(List.of(dispatch));
        given(routeStopRepository.findStopOrderByRouteIdAndStopIdAndDirection(1L, 1L, Direction.OUTBOUND))
                .willReturn(Optional.of(1));
        given(routeStopRepository.findMaxStopOrderByRouteIdAndDirection(1L, Direction.OUTBOUND))
                .willReturn(Optional.of(5));
        given(routeStopRepository.findWithStopByRouteIdAndDirectionAndStopOrder(1L, Direction.OUTBOUND, 1))
                .willReturn(Optional.of(routeStop1));
        given(routeStopRepository.findWithStopByRouteIdAndDirectionAndStopOrder(1L, Direction.OUTBOUND, 2))
                .willReturn(Optional.of(routeStop2));

        // when
        scheduler.tick();
        scheduler.tick();

        // then
        then(gpsLocationRepository).should(org.mockito.Mockito.times(2)).save(any(GpsLocation.class));
    }
}
