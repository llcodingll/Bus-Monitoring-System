package com.bus.monitoringsystem.api.bus.service;

import com.bus.monitoringsystem.api.bus.dto.response.BusSummaryResponse;
import com.bus.monitoringsystem.api.bus.dto.result.BusSummaryResult;
import com.bus.monitoringsystem.api.bus.model.Bus;
import com.bus.monitoringsystem.api.bus.model.BusStatus;
import com.bus.monitoringsystem.api.bus.repository.BusRepository;
import com.bus.monitoringsystem.api.dispatch.model.BusDispatch;
import com.bus.monitoringsystem.api.dispatch.repository.BusDispatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BusService {

    private static final int ONLINE_THRESHOLD_MINUTES = 3;

    private final BusRepository busRepository;
    private final BusDispatchRepository busDispatchRepository;

    /**
     * 전체 버스 요약 정보를 반환한다.
     *
     * @return 버스 요약 응답 목록
     */
    public List<BusSummaryResponse> findAllBusSummaries() {

        List<Bus> buses = busRepository.findAllWithStops();
        Map<Long, BusDispatch> activeDispatchByBusId = buildActiveDispatchMap();

        return buses.stream()
                .map(bus -> toResult(bus, activeDispatchByBusId.get(bus.getId())))
                .map(BusSummaryResponse::from)
                .toList();
    }

    private Map<Long, BusDispatch> buildActiveDispatchMap() {

        return busDispatchRepository.findAllActiveWithRoute().stream()
                .collect(Collectors.toMap(d -> d.getBus().getId(), d -> d));
    }

    private BusSummaryResult toResult(Bus bus, BusDispatch dispatch) {

        return BusSummaryResult.builder()
                .id(bus.getId())
                .busNumber(bus.getBusNumber())
                .routeNumber(dispatch != null ? dispatch.getRoute().getRouteNumber() : null)
                .routeName(dispatch != null ? dispatch.getRoute().getRouteName() : null)
                .currentSpeed(bus.getCurrentSpeed())
                .status(computeStatus(bus.getLastCommunicationAt()))
                .lastCommunicationAt(bus.getLastCommunicationAt())
                .currentStopName(bus.getCurrentStop() != null ? bus.getCurrentStop().getStopName() : null)
                .nextStopName(bus.getNextStop() != null ? bus.getNextStop().getStopName() : null)
                .direction(dispatch != null ? dispatch.getDirection() : null)
                .currentLatitude(bus.getCurrentLatitude())
                .currentLongitude(bus.getCurrentLongitude())
                .build();
    }

    private BusStatus computeStatus(LocalDateTime lastCommunicationAt) {

        if (lastCommunicationAt == null) {
            return BusStatus.OFFLINE;
        }
        return lastCommunicationAt.isAfter(LocalDateTime.now().minusMinutes(ONLINE_THRESHOLD_MINUTES))
                ? BusStatus.ONLINE : BusStatus.OFFLINE;
    }
}
