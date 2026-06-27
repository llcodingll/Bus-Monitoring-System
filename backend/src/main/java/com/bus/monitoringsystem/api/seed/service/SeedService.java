package com.bus.monitoringsystem.api.seed.service;

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
import com.bus.monitoringsystem.api.routestop.repository.RouteStopRepository;
import com.bus.monitoringsystem.api.seed.dto.result.SeedResult;
import com.bus.monitoringsystem.api.simulator.BusSimulationScheduler;
import com.bus.monitoringsystem.api.simulator.cache.ActiveDispatchCache;
import com.bus.monitoringsystem.api.simulator.cache.RouteStopCache;
import com.bus.monitoringsystem.api.stop.model.Stop;
import com.bus.monitoringsystem.api.stop.repository.StopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
public class SeedService {

    private final StopRepository stopRepository;
    private final RouteRepository routeRepository;
    private final RouteStopRepository routeStopRepository;
    private final BusRepository busRepository;
    private final BusDispatchRepository busDispatchRepository;
    private final GpsLocationRepository gpsLocationRepository;
    private final EventRepository eventRepository;
    private final ActiveDispatchCache activeDispatchCache;
    private final RouteStopCache routeStopCache;
    private final BusSimulationScheduler busSimulationScheduler;

    @Transactional
    public SeedResult seed() {

        clearAll();

        Map<String, Stop> stops = insertStops();
        Map<String, Route> routes = insertRoutes(stops);
        insertRouteStops(routes, stops);
        Map<String, Bus> buses = insertBuses(stops);
        Map<String, BusDispatch> dispatches = insertDispatches(buses, routes);
        insertGpsLocations(buses, dispatches);
        List<Event> events = insertEvents(buses, dispatches, routes);

        busSimulationScheduler.clearState();
        activeDispatchCache.load();
        routeStopCache.load();

        return SeedResult.builder()
                .busCount(buses.size())
                .routeCount(routes.size())
                .stopCount(stops.size())
                .eventCount(events.size())
                .build();
    }

    private void clearAll() {

        eventRepository.deleteAllInBatch();
        gpsLocationRepository.deleteAllInBatch();
        busDispatchRepository.deleteAllInBatch();
        routeStopRepository.deleteAllInBatch();
        busRepository.deleteAllInBatch();
        routeRepository.deleteAllInBatch();
        stopRepository.deleteAllInBatch();
    }

    private Map<String, Stop> insertStops() {

        List<Stop> saved = stopRepository.saveAll(List.of(
                stop("상계동",           "37.6760", "127.0573"),
                stop("노원역",           "37.6558", "127.0566"),
                stop("미아사거리",        "37.6292", "127.0253"),
                stop("성신여대입구",      "37.5930", "127.0175"),
                stop("한성대입구",        "37.5880", "127.0095"),
                stop("혜화역",           "37.5820", "127.0015"),
                stop("서울역",           "37.5546", "126.9706"),
                stop("중계동",           "37.6503", "127.0753"),
                stop("광운대역",         "37.6241", "127.0580"),
                stop("석계역",           "37.6136", "127.0627"),
                stop("을지로3가",        "37.5661", "126.9910"),
                stop("신촌역",           "37.5596", "126.9430"),
                stop("홍대입구역",       "37.5576", "126.9245"),
                stop("합정역",           "37.5494", "126.9143"),
                stop("성수역",           "37.5446", "127.0561"),
                stop("건대입구역",       "37.5404", "127.0694"),
                stop("잠실역",           "37.5133", "127.1000"),
                stop("도봉산역",         "37.6894", "127.0476"),
                stop("방학역",           "37.6594", "127.0380"),
                stop("창동역",           "37.6528", "127.0471"),
                stop("길음역",           "37.5930", "127.0255"),
                stop("시청앞",           "37.5659", "126.9772"),
                stop("사당역",           "37.4762", "126.9814"),
                stop("이수역",           "37.4851", "126.9816"),
                stop("신림역",           "37.4840", "126.9296"),
                stop("강남역",           "37.4979", "127.0276"),
                stop("교대역",           "37.4934", "127.0142"),
                stop("서초역",           "37.4836", "127.0126"),
                stop("여의나루역",        "37.5281", "126.9322"),
                stop("여의도역",         "37.5216", "126.9241"),
                stop("수유역",           "37.6388", "127.0255"),
                stop("명동",             "37.5601", "126.9930"),
                stop("구로역",           "37.5026", "126.8819"),
                stop("구로디지털단지역", "37.4851", "126.9012"),
                stop("대림역",           "37.4921", "126.8977"),
                stop("영등포역",         "37.5155", "126.9057")
        ));

        return saved.stream().collect(toMap(Stop::getStopName, s -> s));
    }

    private Map<String, Route> insertRoutes(Map<String, Stop> s) {

        List<Route> saved = routeRepository.saveAll(List.of(
                Route.builder().routeNumber("370").routeName("상계동 - 서울역")
                        .startStop(s.get("상계동")).endStop(s.get("서울역")).build(),
                Route.builder().routeNumber("471").routeName("중계동 - 서울역")
                        .startStop(s.get("중계동")).endStop(s.get("서울역")).build(),
                Route.builder().routeNumber("261").routeName("신촌역 - 잠실역")
                        .startStop(s.get("신촌역")).endStop(s.get("잠실역")).build(),
                Route.builder().routeNumber("143").routeName("도봉산역 - 서울역")
                        .startStop(s.get("도봉산역")).endStop(s.get("서울역")).build(),
                Route.builder().routeNumber("500").routeName("시청앞 - 신림역")
                        .startStop(s.get("시청앞")).endStop(s.get("신림역")).build(),
                Route.builder().routeNumber("702").routeName("강남역 - 여의도역")
                        .startStop(s.get("강남역")).endStop(s.get("여의도역")).build(),
                Route.builder().routeNumber("340").routeName("수유역 - 명동")
                        .startStop(s.get("수유역")).endStop(s.get("명동")).build(),
                Route.builder().routeNumber("420").routeName("구로역 - 서울역")
                        .startStop(s.get("구로역")).endStop(s.get("서울역")).build()
        ));

        return saved.stream().collect(toMap(Route::getRouteNumber, r -> r));
    }

    private void insertRouteStops(Map<String, Route> routes, Map<String, Stop> s) {

        Route r370 = routes.get("370");
        Route r471 = routes.get("471");
        Route r261 = routes.get("261");
        Route r143 = routes.get("143");
        Route r500 = routes.get("500");
        Route r702 = routes.get("702");
        Route r340 = routes.get("340");
        Route r420 = routes.get("420");

        routeStopRepository.saveAll(List.of(
                rs(r370, s.get("상계동"),           1, Direction.OUTBOUND),
                rs(r370, s.get("노원역"),           2, Direction.OUTBOUND),
                rs(r370, s.get("미아사거리"),        3, Direction.OUTBOUND),
                rs(r370, s.get("성신여대입구"),      4, Direction.OUTBOUND),
                rs(r370, s.get("한성대입구"),        5, Direction.OUTBOUND),
                rs(r370, s.get("혜화역"),           6, Direction.OUTBOUND),
                rs(r370, s.get("서울역"),           7, Direction.OUTBOUND),
                rs(r370, s.get("서울역"),           1, Direction.INBOUND),
                rs(r370, s.get("혜화역"),           2, Direction.INBOUND),
                rs(r370, s.get("한성대입구"),        3, Direction.INBOUND),
                rs(r370, s.get("성신여대입구"),      4, Direction.INBOUND),
                rs(r370, s.get("미아사거리"),        5, Direction.INBOUND),
                rs(r370, s.get("노원역"),           6, Direction.INBOUND),
                rs(r370, s.get("상계동"),           7, Direction.INBOUND),

                rs(r471, s.get("중계동"),           1, Direction.OUTBOUND),
                rs(r471, s.get("광운대역"),         2, Direction.OUTBOUND),
                rs(r471, s.get("석계역"),           3, Direction.OUTBOUND),
                rs(r471, s.get("혜화역"),           4, Direction.OUTBOUND),
                rs(r471, s.get("을지로3가"),        5, Direction.OUTBOUND),
                rs(r471, s.get("서울역"),           6, Direction.OUTBOUND),
                rs(r471, s.get("서울역"),           1, Direction.INBOUND),
                rs(r471, s.get("을지로3가"),        2, Direction.INBOUND),
                rs(r471, s.get("혜화역"),           3, Direction.INBOUND),
                rs(r471, s.get("석계역"),           4, Direction.INBOUND),
                rs(r471, s.get("광운대역"),         5, Direction.INBOUND),
                rs(r471, s.get("중계동"),           6, Direction.INBOUND),

                rs(r261, s.get("신촌역"),           1, Direction.OUTBOUND),
                rs(r261, s.get("홍대입구역"),       2, Direction.OUTBOUND),
                rs(r261, s.get("합정역"),           3, Direction.OUTBOUND),
                rs(r261, s.get("성수역"),           4, Direction.OUTBOUND),
                rs(r261, s.get("건대입구역"),       5, Direction.OUTBOUND),
                rs(r261, s.get("잠실역"),           6, Direction.OUTBOUND),
                rs(r261, s.get("잠실역"),           1, Direction.INBOUND),
                rs(r261, s.get("건대입구역"),       2, Direction.INBOUND),
                rs(r261, s.get("성수역"),           3, Direction.INBOUND),
                rs(r261, s.get("합정역"),           4, Direction.INBOUND),
                rs(r261, s.get("홍대입구역"),       5, Direction.INBOUND),
                rs(r261, s.get("신촌역"),           6, Direction.INBOUND),

                rs(r143, s.get("도봉산역"),         1, Direction.OUTBOUND),
                rs(r143, s.get("방학역"),           2, Direction.OUTBOUND),
                rs(r143, s.get("창동역"),           3, Direction.OUTBOUND),
                rs(r143, s.get("노원역"),           4, Direction.OUTBOUND),
                rs(r143, s.get("길음역"),           5, Direction.OUTBOUND),
                rs(r143, s.get("서울역"),           6, Direction.OUTBOUND),
                rs(r143, s.get("서울역"),           1, Direction.INBOUND),
                rs(r143, s.get("길음역"),           2, Direction.INBOUND),
                rs(r143, s.get("노원역"),           3, Direction.INBOUND),
                rs(r143, s.get("창동역"),           4, Direction.INBOUND),
                rs(r143, s.get("방학역"),           5, Direction.INBOUND),
                rs(r143, s.get("도봉산역"),         6, Direction.INBOUND),

                rs(r500, s.get("시청앞"),           1, Direction.OUTBOUND),
                rs(r500, s.get("사당역"),           2, Direction.OUTBOUND),
                rs(r500, s.get("이수역"),           3, Direction.OUTBOUND),
                rs(r500, s.get("신림역"),           4, Direction.OUTBOUND),
                rs(r500, s.get("신림역"),           1, Direction.INBOUND),
                rs(r500, s.get("이수역"),           2, Direction.INBOUND),
                rs(r500, s.get("사당역"),           3, Direction.INBOUND),
                rs(r500, s.get("시청앞"),           4, Direction.INBOUND),

                rs(r702, s.get("강남역"),           1, Direction.OUTBOUND),
                rs(r702, s.get("교대역"),           2, Direction.OUTBOUND),
                rs(r702, s.get("서초역"),           3, Direction.OUTBOUND),
                rs(r702, s.get("여의나루역"),        4, Direction.OUTBOUND),
                rs(r702, s.get("여의도역"),         5, Direction.OUTBOUND),
                rs(r702, s.get("여의도역"),         1, Direction.INBOUND),
                rs(r702, s.get("여의나루역"),        2, Direction.INBOUND),
                rs(r702, s.get("서초역"),           3, Direction.INBOUND),
                rs(r702, s.get("교대역"),           4, Direction.INBOUND),
                rs(r702, s.get("강남역"),           5, Direction.INBOUND),

                rs(r340, s.get("수유역"),           1, Direction.OUTBOUND),
                rs(r340, s.get("미아사거리"),        2, Direction.OUTBOUND),
                rs(r340, s.get("길음역"),           3, Direction.OUTBOUND),
                rs(r340, s.get("성신여대입구"),      4, Direction.OUTBOUND),
                rs(r340, s.get("혜화역"),           5, Direction.OUTBOUND),
                rs(r340, s.get("명동"),             6, Direction.OUTBOUND),
                rs(r340, s.get("명동"),             1, Direction.INBOUND),
                rs(r340, s.get("혜화역"),           2, Direction.INBOUND),
                rs(r340, s.get("성신여대입구"),      3, Direction.INBOUND),
                rs(r340, s.get("길음역"),           4, Direction.INBOUND),
                rs(r340, s.get("미아사거리"),        5, Direction.INBOUND),
                rs(r340, s.get("수유역"),           6, Direction.INBOUND),

                rs(r420, s.get("구로역"),           1, Direction.OUTBOUND),
                rs(r420, s.get("구로디지털단지역"), 2, Direction.OUTBOUND),
                rs(r420, s.get("대림역"),           3, Direction.OUTBOUND),
                rs(r420, s.get("영등포역"),         4, Direction.OUTBOUND),
                rs(r420, s.get("서울역"),           5, Direction.OUTBOUND),
                rs(r420, s.get("서울역"),           1, Direction.INBOUND),
                rs(r420, s.get("영등포역"),         2, Direction.INBOUND),
                rs(r420, s.get("대림역"),           3, Direction.INBOUND),
                rs(r420, s.get("구로디지털단지역"), 4, Direction.INBOUND),
                rs(r420, s.get("구로역"),           5, Direction.INBOUND)
        ));
    }

    private Map<String, Bus> insertBuses(Map<String, Stop> s) {

        LocalDateTime now = LocalDateTime.now();

        List<Bus> saved = busRepository.saveAll(List.of(
                bus("서울75바1234", 42, "37.6558", "127.0566", s.get("노원역"),           s.get("미아사거리"),        now.minusSeconds(45)),
                bus("서울75바2345", 28, "37.5930", "127.0175", s.get("성신여대입구"),     s.get("한성대입구"),        now.minusSeconds(30)),
                bus("서울75바3456",  0, "37.5820", "127.0015", s.get("혜화역"),           s.get("한성대입구"),        now.minusSeconds(480)),
                bus("서울74바4567", 35, "37.6241", "127.0580", s.get("광운대역"),         s.get("석계역"),            now.minusSeconds(60)),
                bus("서울74바5678", 22, "37.5661", "126.9910", s.get("을지로3가"),        s.get("혜화역"),            now.minusSeconds(20)),
                bus("서울73바6789", 31, "37.5576", "126.9245", s.get("홍대입구역"),       s.get("합정역"),            now.minusSeconds(35)),
                bus("서울73바7890",  0, "37.5446", "127.0561", s.get("성수역"),           s.get("건대입구역"),        now.minusSeconds(720)),
                bus("서울72바8901", 48, "37.6594", "127.0380", s.get("방학역"),           s.get("창동역"),            now.minusSeconds(50)),
                bus("서울72바9012", 37, "37.5930", "127.0255", s.get("길음역"),           s.get("노원역"),            now.minusSeconds(25)),
                bus("서울71나1234", 25, "37.4762", "126.9814", s.get("사당역"),           s.get("이수역"),            now.minusSeconds(40)),
                bus("서울71나2345", 18, "37.4851", "126.9816", s.get("이수역"),           s.get("사당역"),            now.minusSeconds(15)),
                bus("서울80가1111", 38, "37.4934", "127.0142", s.get("교대역"),           s.get("서초역"),            now.minusSeconds(25)),
                bus("서울80가2222", 45, "37.4836", "127.0126", s.get("서초역"),           s.get("여의나루역"),        now.minusSeconds(15)),
                bus("서울80가3333",  0, "37.5281", "126.9322", s.get("여의나루역"),        s.get("서초역"),            now.minusSeconds(450)),
                bus("서울79나1111", 32, "37.6292", "127.0253", s.get("미아사거리"),        s.get("길음역"),            now.minusSeconds(30)),
                bus("서울79나2222", 27, "37.5930", "127.0175", s.get("성신여대입구"),     s.get("혜화역"),            now.minusSeconds(20)),
                bus("서울79나3333", 35, "37.5820", "127.0015", s.get("혜화역"),           s.get("성신여대입구"),      now.minusSeconds(10)),
                bus("서울78다1111", 29, "37.5026", "126.8819", s.get("구로역"),           s.get("구로디지털단지역"), now.minusSeconds(40)),
                bus("서울78다2222", 41, "37.5155", "126.9057", s.get("영등포역"),         s.get("서울역"),            now.minusSeconds(35)),
                bus("서울78다3333",  0, "37.4921", "126.8977", s.get("대림역"),           s.get("구로디지털단지역"), now.minusSeconds(600))
        ));

        return saved.stream().collect(toMap(Bus::getBusNumber, b -> b));
    }

    private Map<String, BusDispatch> insertDispatches(Map<String, Bus> buses, Map<String, Route> routes) {

        LocalDate today = LocalDate.now();
        LocalDateTime startedAt = LocalDateTime.now().minusHours(3);

        List<BusDispatch> saved = busDispatchRepository.saveAll(List.of(
                dispatch(buses.get("서울75바1234"), routes.get("370"), Direction.OUTBOUND, today, startedAt),
                dispatch(buses.get("서울75바2345"), routes.get("370"), Direction.OUTBOUND, today, startedAt),
                dispatch(buses.get("서울75바3456"), routes.get("370"), Direction.INBOUND,  today, startedAt),
                dispatch(buses.get("서울74바4567"), routes.get("471"), Direction.OUTBOUND, today, startedAt),
                dispatch(buses.get("서울74바5678"), routes.get("471"), Direction.INBOUND,  today, startedAt),
                dispatch(buses.get("서울73바6789"), routes.get("261"), Direction.OUTBOUND, today, startedAt),
                dispatch(buses.get("서울73바7890"), routes.get("261"), Direction.OUTBOUND, today, startedAt),
                dispatch(buses.get("서울72바8901"), routes.get("143"), Direction.OUTBOUND, today, startedAt),
                dispatch(buses.get("서울72바9012"), routes.get("143"), Direction.INBOUND,  today, startedAt),
                dispatch(buses.get("서울71나1234"), routes.get("500"), Direction.OUTBOUND, today, startedAt),
                dispatch(buses.get("서울71나2345"), routes.get("500"), Direction.INBOUND,  today, startedAt),
                dispatch(buses.get("서울80가1111"), routes.get("702"), Direction.OUTBOUND, today, startedAt),
                dispatch(buses.get("서울80가2222"), routes.get("702"), Direction.OUTBOUND, today, startedAt),
                dispatch(buses.get("서울80가3333"), routes.get("702"), Direction.INBOUND,  today, startedAt),
                dispatch(buses.get("서울79나1111"), routes.get("340"), Direction.OUTBOUND, today, startedAt),
                dispatch(buses.get("서울79나2222"), routes.get("340"), Direction.OUTBOUND, today, startedAt),
                dispatch(buses.get("서울79나3333"), routes.get("340"), Direction.INBOUND,  today, startedAt),
                dispatch(buses.get("서울78다1111"), routes.get("420"), Direction.OUTBOUND, today, startedAt),
                dispatch(buses.get("서울78다2222"), routes.get("420"), Direction.OUTBOUND, today, startedAt),
                dispatch(buses.get("서울78다3333"), routes.get("420"), Direction.INBOUND,  today, startedAt)
        ));

        return saved.stream()
                .collect(toMap(d -> d.getBus().getBusNumber(), d -> d));
    }

    private void insertGpsLocations(Map<String, Bus> buses, Map<String, BusDispatch> dispatches) {

        LocalDateTime now = LocalDateTime.now();

        gpsLocationRepository.saveAll(List.of(
                gps(buses.get("서울75바1234"), dispatches.get("서울75바1234"), "37.6558", "127.0566", 42, now.minusSeconds(45)),
                gps(buses.get("서울75바1234"), dispatches.get("서울75바1234"), "37.6520", "127.0540", 38, now.minusSeconds(75)),
                gps(buses.get("서울75바2345"), dispatches.get("서울75바2345"), "37.5930", "127.0175", 28, now.minusSeconds(30)),
                gps(buses.get("서울75바2345"), dispatches.get("서울75바2345"), "37.5950", "127.0200", 32, now.minusSeconds(60)),
                gps(buses.get("서울75바3456"), dispatches.get("서울75바3456"), "37.5820", "127.0015",  0, now.minusSeconds(480)),
                gps(buses.get("서울74바4567"), dispatches.get("서울74바4567"), "37.6241", "127.0580", 35, now.minusSeconds(60)),
                gps(buses.get("서울74바5678"), dispatches.get("서울74바5678"), "37.5661", "126.9910", 22, now.minusSeconds(20)),
                gps(buses.get("서울73바6789"), dispatches.get("서울73바6789"), "37.5576", "126.9245", 31, now.minusSeconds(35)),
                gps(buses.get("서울73바7890"), dispatches.get("서울73바7890"), "37.5446", "127.0561",  0, now.minusSeconds(720)),
                gps(buses.get("서울72바8901"), dispatches.get("서울72바8901"), "37.6594", "127.0380", 48, now.minusSeconds(50)),
                gps(buses.get("서울72바9012"), dispatches.get("서울72바9012"), "37.5930", "127.0255", 37, now.minusSeconds(25)),
                gps(buses.get("서울71나1234"), dispatches.get("서울71나1234"), "37.4762", "126.9814", 25, now.minusSeconds(40)),
                gps(buses.get("서울71나2345"), dispatches.get("서울71나2345"), "37.4851", "126.9816", 18, now.minusSeconds(15)),
                gps(buses.get("서울80가1111"), dispatches.get("서울80가1111"), "37.4934", "127.0142", 38, now.minusSeconds(25)),
                gps(buses.get("서울80가2222"), dispatches.get("서울80가2222"), "37.4836", "127.0126", 45, now.minusSeconds(15)),
                gps(buses.get("서울80가3333"), dispatches.get("서울80가3333"), "37.5281", "126.9322",  0, now.minusSeconds(450)),
                gps(buses.get("서울79나1111"), dispatches.get("서울79나1111"), "37.6292", "127.0253", 32, now.minusSeconds(30)),
                gps(buses.get("서울79나2222"), dispatches.get("서울79나2222"), "37.5930", "127.0175", 27, now.minusSeconds(20)),
                gps(buses.get("서울79나3333"), dispatches.get("서울79나3333"), "37.5820", "127.0015", 35, now.minusSeconds(10)),
                gps(buses.get("서울78다1111"), dispatches.get("서울78다1111"), "37.5026", "126.8819", 29, now.minusSeconds(40)),
                gps(buses.get("서울78다2222"), dispatches.get("서울78다2222"), "37.5155", "126.9057", 41, now.minusSeconds(35)),
                gps(buses.get("서울78다3333"), dispatches.get("서울78다3333"), "37.4921", "126.8977",  0, now.minusSeconds(600))
        ));
    }

    private List<Event> insertEvents(Map<String, Bus> buses,
                                     Map<String, BusDispatch> dispatches,
                                     Map<String, Route> routes) {

        LocalDateTime now = LocalDateTime.now();

        return eventRepository.saveAll(List.of(
                event(buses.get("서울75바1234"), dispatches.get("서울75바1234"), routes.get("370"),
                        EventType.SUDDEN_BRAKE,        Severity.HIGH,   "37.6480", "127.0510", now.minusMinutes(20)),
                event(buses.get("서울75바2345"), dispatches.get("서울75바2345"), routes.get("370"),
                        EventType.SUDDEN_DECELERATION, Severity.HIGH,   "37.5960", "127.0190", now.minusMinutes(35)),
                event(buses.get("서울74바4567"), dispatches.get("서울74바4567"), routes.get("471"),
                        EventType.SUDDEN_ACCELERATION, Severity.HIGH,   "37.6200", "127.0560", now.minusMinutes(45)),
                event(buses.get("서울74바5678"), dispatches.get("서울74바5678"), routes.get("471"),
                        EventType.IMPACT,               Severity.HIGH,   "37.5640", "126.9890", now.minusMinutes(8)),
                event(buses.get("서울73바6789"), dispatches.get("서울73바6789"), routes.get("261"),
                        EventType.IMPACT,               Severity.MEDIUM, "37.5560", "126.9230", now.minusMinutes(70)),
                event(buses.get("서울73바7890"), dispatches.get("서울73바7890"), routes.get("261"),
                        EventType.SUDDEN_ACCELERATION,  Severity.LOW,   "37.5430", "127.0550", now.minusMinutes(90)),
                event(buses.get("서울72바8901"), dispatches.get("서울72바8901"), routes.get("143"),
                        EventType.SUDDEN_DECELERATION,  Severity.MEDIUM, "37.6580", "127.0370", now.minusMinutes(100)),
                event(buses.get("서울72바9012"), dispatches.get("서울72바9012"), routes.get("143"),
                        EventType.SUDDEN_BRAKE,         Severity.HIGH,   "37.5910", "127.0240", now.minusMinutes(3)),
                event(buses.get("서울71나1234"), dispatches.get("서울71나1234"), routes.get("500"),
                        EventType.SUDDEN_START,         Severity.LOW,    "37.4750", "126.9800", now.minusMinutes(150)),
                event(buses.get("서울71나2345"), dispatches.get("서울71나2345"), routes.get("500"),
                        EventType.SUDDEN_BRAKE,         Severity.MEDIUM, "37.4860", "126.9820", now.minusMinutes(12)),
                event(buses.get("서울80가1111"), dispatches.get("서울80가1111"), routes.get("702"),
                        EventType.SUDDEN_BRAKE,         Severity.HIGH,   "37.4920", "127.0150", now.minusMinutes(5)),
                event(buses.get("서울80가2222"), dispatches.get("서울80가2222"), routes.get("702"),
                        EventType.SUDDEN_ACCELERATION,  Severity.MEDIUM, "37.4840", "127.0130", now.minusMinutes(30)),
                event(buses.get("서울80가3333"), dispatches.get("서울80가3333"), routes.get("702"),
                        EventType.SUDDEN_START,         Severity.HIGH,   "37.5270", "126.9310", now.minusMinutes(35)),
                event(buses.get("서울79나1111"), dispatches.get("서울79나1111"), routes.get("340"),
                        EventType.IMPACT,               Severity.HIGH,   "37.6280", "127.0240", now.minusMinutes(15)),
                event(buses.get("서울79나2222"), dispatches.get("서울79나2222"), routes.get("340"),
                        EventType.SUDDEN_BRAKE,         Severity.LOW,    "37.5920", "127.0160", now.minusMinutes(60)),
                event(buses.get("서울79나3333"), dispatches.get("서울79나3333"), routes.get("340"),
                        EventType.SUDDEN_DECELERATION,  Severity.MEDIUM, "37.5810", "127.0000", now.minusMinutes(55)),
                event(buses.get("서울78다1111"), dispatches.get("서울78다1111"), routes.get("420"),
                        EventType.SUDDEN_DECELERATION,  Severity.HIGH,   "37.5010", "126.8800", now.minusMinutes(10)),
                event(buses.get("서울78다2222"), dispatches.get("서울78다2222"), routes.get("420"),
                        EventType.SUDDEN_START,         Severity.MEDIUM, "37.5140", "126.9040", now.minusMinutes(40)),
                event(buses.get("서울78다3333"), dispatches.get("서울78다3333"), routes.get("420"),
                        EventType.IMPACT,               Severity.LOW,    "37.4910", "126.8960", now.minusMinutes(120)),
                event(buses.get("서울75바1234"), dispatches.get("서울75바1234"), routes.get("370"),
                        EventType.SUDDEN_ACCELERATION,  Severity.MEDIUM, "37.6500", "127.0520", now.minusMinutes(180))
        ));
    }

    private Stop stop(String name, String lat, String lng) {

        return Stop.builder()
                .stopName(name)
                .latitude(new BigDecimal(lat))
                .longitude(new BigDecimal(lng))
                .build();
    }

    private RouteStop rs(Route route, Stop stop, int order, Direction direction) {

        return RouteStop.builder()
                .route(route).stop(stop).stopOrder(order).direction(direction).build();
    }

    private Bus bus(String busNumber, int speed, String lat, String lng,
                    Stop currentStop, Stop nextStop, LocalDateTime lastCommunicationAt) {

        return Bus.builder()
                .busNumber(busNumber)
                .currentSpeed(speed)
                .currentLatitude(new BigDecimal(lat))
                .currentLongitude(new BigDecimal(lng))
                .currentStop(currentStop)
                .nextStop(nextStop)
                .lastCommunicationAt(lastCommunicationAt)
                .build();
    }

    private BusDispatch dispatch(Bus bus, Route route, Direction direction,
                                 LocalDate dispatchedDate, LocalDateTime operationStartedAt) {

        return BusDispatch.builder()
                .bus(bus).route(route).direction(direction)
                .dispatchedDate(dispatchedDate)
                .operationStartedAt(operationStartedAt)
                .build();
    }

    private GpsLocation gps(Bus bus, BusDispatch dispatch, String lat, String lng,
                             int speed, LocalDateTime recordedAt) {

        return GpsLocation.builder()
                .bus(bus).dispatch(dispatch)
                .latitude(new BigDecimal(lat))
                .longitude(new BigDecimal(lng))
                .speed(speed)
                .recordedAt(recordedAt)
                .build();
    }

    private Event event(Bus bus, BusDispatch dispatch, Route route,
                        EventType type, Severity severity, String lat, String lng,
                        LocalDateTime occurredAt) {

        return Event.builder()
                .bus(bus).dispatch(dispatch).route(route)
                .eventType(type).severity(severity)
                .latitude(new BigDecimal(lat))
                .longitude(new BigDecimal(lng))
                .occurredAt(occurredAt)
                .build();
    }
}
