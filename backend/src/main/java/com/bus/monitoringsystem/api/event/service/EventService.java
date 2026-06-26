package com.bus.monitoringsystem.api.event.service;

import com.bus.monitoringsystem.api.event.dto.response.EventSummaryResponse;
import com.bus.monitoringsystem.api.event.dto.result.EventSummaryResult;
import com.bus.monitoringsystem.api.event.repository.EventRepository;
import com.bus.monitoringsystem.common.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;

    public PageResponse<EventSummaryResponse> findRecentEvents(int page, int size) {

        return PageResponse.from(
                eventRepository.findAllWithBusAndRoute(PageRequest.of(page, size)),
                event -> EventSummaryResponse.from(EventSummaryResult.from(event))
        );
    }

    public PageResponse<EventSummaryResponse> findRecentEventsByBusId(Long busId, int page, int size) {

        return PageResponse.from(
                eventRepository.findAllByBusIdWithBusAndRoute(busId, PageRequest.of(page, size)),
                event -> EventSummaryResponse.from(EventSummaryResult.from(event))
        );
    }
}
