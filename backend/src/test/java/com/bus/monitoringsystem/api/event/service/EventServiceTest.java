package com.bus.monitoringsystem.api.event.service;

import com.bus.monitoringsystem.api.bus.model.Bus;
import com.bus.monitoringsystem.api.dispatch.model.BusDispatch;
import com.bus.monitoringsystem.api.event.dto.response.EventSummaryResponse;
import com.bus.monitoringsystem.api.event.model.Event;
import com.bus.monitoringsystem.api.event.model.EventType;
import com.bus.monitoringsystem.api.event.model.Severity;
import com.bus.monitoringsystem.api.event.repository.EventRepository;
import com.bus.monitoringsystem.api.route.model.Route;
import com.bus.monitoringsystem.common.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventService")
class EventServiceTest {

    @InjectMocks
    private EventService eventService;

    @Mock
    private EventRepository eventRepository;

    @Test
    @DisplayName("이벤트가 존재하면 페이지 응답을 반환한다")
    void findRecentEvents_returnsPage_whenEventsExist() {

        // given
        Bus bus = mock(Bus.class);
        given(bus.getId()).willReturn(1L);
        given(bus.getBusNumber()).willReturn("143-1");

        Route route = mock(Route.class);
        given(route.getRouteNumber()).willReturn("143");
        given(route.getRouteName()).willReturn("143번");

        Event event = Event.builder()
                .bus(bus)
                .dispatch(mock(BusDispatch.class))
                .route(route)
                .eventType(EventType.SUDDEN_BRAKE)
                .severity(Severity.HIGH)
                .latitude(new BigDecimal("37.5000000"))
                .longitude(new BigDecimal("127.0000000"))
                .occurredAt(LocalDateTime.now())
                .build();

        PageRequest pageable = PageRequest.of(0, 20);
        given(eventRepository.findAllWithBusAndRoute(any())).willReturn(new PageImpl<>(List.of(event), pageable, 1));

        // when
        PageResponse<EventSummaryResponse> result = eventService.findRecentEvents(0, 20);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getCurrentPage()).isEqualTo(0);
        then(eventRepository).should().findAllWithBusAndRoute(any());
    }

    @Test
    @DisplayName("이벤트가 없으면 빈 페이지를 반환한다")
    void findRecentEvents_returnsEmptyPage_whenNoEvents() {

        // given
        PageRequest pageable = PageRequest.of(0, 20);
        given(eventRepository.findAllWithBusAndRoute(any())).willReturn(new PageImpl<>(List.of(), pageable, 0));

        // when
        PageResponse<EventSummaryResponse> result = eventService.findRecentEvents(0, 20);

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }
}
