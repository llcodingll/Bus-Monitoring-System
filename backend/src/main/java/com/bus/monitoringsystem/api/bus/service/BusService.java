package com.bus.monitoringsystem.api.bus.service;

import com.bus.monitoringsystem.api.bus.dto.response.BusDetailResponse;
import com.bus.monitoringsystem.api.bus.dto.response.BusSummaryResponse;
import com.bus.monitoringsystem.api.bus.dto.result.BusDetailResult;
import com.bus.monitoringsystem.api.bus.dto.result.BusSummaryResult;
import com.bus.monitoringsystem.api.bus.model.Bus;
import com.bus.monitoringsystem.api.bus.policy.OnlineStatusPolicy;
import com.bus.monitoringsystem.api.bus.repository.BusRepository;
import com.bus.monitoringsystem.api.dispatch.model.BusDispatch;
import com.bus.monitoringsystem.api.dispatch.repository.BusDispatchRepository;
import com.bus.monitoringsystem.common.BaseException;
import com.bus.monitoringsystem.common.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BusService {

    private final BusRepository busRepository;
    private final BusDispatchRepository busDispatchRepository;
    private final OnlineStatusPolicy onlineStatusPolicy;

    public List<BusSummaryResponse> findAllBusSummaries() {

        List<Bus> buses = busRepository.findAllWithStops();
        Map<Long, BusDispatch> activeDispatchByBusId = buildActiveDispatchMap();
        Instant now = Instant.now();

        return buses.stream()
                .map(bus -> toResult(bus, activeDispatchByBusId.get(bus.getId()), now))
                .map(BusSummaryResponse::from)
                .toList();
    }

    private Map<Long, BusDispatch> buildActiveDispatchMap() {

        return busDispatchRepository.findAllActiveWithRoute().stream()
                .collect(toMap(d -> d.getBus().getId(), d -> d));
    }

    public BusDetailResponse findDetail(Long id) {

        Bus bus = busRepository.findByIdWithStops(id)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.BUS_NOT_FOUND));

        BusDispatch dispatch = busDispatchRepository.findAllActiveWithRoute().stream()
                .filter(d -> d.getBus().getId().equals(id))
                .findFirst()
                .orElse(null);

        Instant now = Instant.now();
        Instant lastCommunicatedAt = bus.getLastCommunicationAt() != null
                ? bus.getLastCommunicationAt().atZone(ZoneId.systemDefault()).toInstant()
                : null;

        BusDetailResult result = BusDetailResult.builder()
                .id(bus.getId())
                .busNumber(bus.getBusNumber())
                .routeNumber(dispatch != null ? dispatch.getRoute().getRouteNumber() : null)
                .routeName(dispatch != null ? dispatch.getRoute().getRouteName() : null)
                .currentSpeed(bus.getCurrentSpeed())
                .status(onlineStatusPolicy.resolve(lastCommunicatedAt, now))
                .lastCommunicationAt(bus.getLastCommunicationAt())
                .currentStopName(bus.getCurrentStop() != null ? bus.getCurrentStop().getStopName() : null)
                .nextStopName(bus.getNextStop() != null ? bus.getNextStop().getStopName() : null)
                .direction(dispatch != null ? dispatch.getDirection() : null)
                .operationStartedAt(dispatch != null ? dispatch.getOperationStartedAt() : null)
                .currentLatitude(bus.getCurrentLatitude())
                .currentLongitude(bus.getCurrentLongitude())
                .build();

        return BusDetailResponse.from(result);
    }

    private BusSummaryResult toResult(Bus bus, BusDispatch dispatch, Instant now) {

        Instant lastCommunicatedAt = bus.getLastCommunicationAt() != null
                ? bus.getLastCommunicationAt().atZone(ZoneId.systemDefault()).toInstant()
                : null;

        return BusSummaryResult.builder()
                .id(bus.getId())
                .busNumber(bus.getBusNumber())
                .routeNumber(dispatch != null ? dispatch.getRoute().getRouteNumber() : null)
                .routeName(dispatch != null ? dispatch.getRoute().getRouteName() : null)
                .currentSpeed(bus.getCurrentSpeed())
                .status(onlineStatusPolicy.resolve(lastCommunicatedAt, now))
                .lastCommunicationAt(bus.getLastCommunicationAt())
                .currentStopName(bus.getCurrentStop() != null ? bus.getCurrentStop().getStopName() : null)
                .nextStopName(bus.getNextStop() != null ? bus.getNextStop().getStopName() : null)
                .direction(dispatch != null ? dispatch.getDirection() : null)
                .currentLatitude(bus.getCurrentLatitude())
                .currentLongitude(bus.getCurrentLongitude())
                .build();
    }
}
