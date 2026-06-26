package com.bus.monitoringsystem.api.event.controller;

import com.bus.monitoringsystem.api.event.dto.response.EventSummaryResponse;
import com.bus.monitoringsystem.api.event.model.EventType;
import com.bus.monitoringsystem.api.event.model.Severity;
import com.bus.monitoringsystem.api.event.service.EventService;
import com.bus.monitoringsystem.common.BaseResponse;
import com.bus.monitoringsystem.common.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventController")
class EventControllerTest {

    @InjectMocks
    private EventController eventController;

    @Mock
    private EventService eventService;

    @Test
    @DisplayName("이벤트 조회 시 200과 페이지 응답을 반환한다")
    void findRecentEvents_returns200_withPageResponse() {

        // given
        EventSummaryResponse response = EventSummaryResponse.builder()
                .id(1L)
                .busId(1L)
                .busNumber("143-1")
                .routeNumber("143")
                .routeName("143번")
                .eventType(EventType.SUDDEN_BRAKE.name())
                .severity(Severity.HIGH.name())
                .latitude(new BigDecimal("37.5000000"))
                .longitude(new BigDecimal("127.0000000"))
                .occurredAt(LocalDateTime.now())
                .build();

        PageResponse<EventSummaryResponse> page = PageResponse.<EventSummaryResponse>builder()
                .content(List.of(response))
                .totalElements(1)
                .totalPages(1)
                .currentPage(0)
                .size(20)
                .build();

        given(eventService.findRecentEvents(0, 20)).willReturn(page);

        // when
        ResponseEntity<BaseResponse<PageResponse<EventSummaryResponse>>> result =
                eventController.findRecentEvents(0, 20);

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getResult().getContent()).hasSize(1);
        assertThat(result.getBody().getResult().getTotalElements()).isEqualTo(1);
        then(eventService).should().findRecentEvents(0, 20);
    }

    @Test
    @DisplayName("이벤트가 없으면 200과 빈 content를 반환한다")
    void findRecentEvents_returns200_withEmptyContent() {

        // given
        PageResponse<EventSummaryResponse> emptyPage = PageResponse.<EventSummaryResponse>builder()
                .content(List.of())
                .totalElements(0)
                .totalPages(0)
                .currentPage(0)
                .size(20)
                .build();

        given(eventService.findRecentEvents(0, 20)).willReturn(emptyPage);

        // when
        ResponseEntity<BaseResponse<PageResponse<EventSummaryResponse>>> result =
                eventController.findRecentEvents(0, 20);

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getResult().getContent()).isEmpty();
        assertThat(result.getBody().getResult().getTotalElements()).isEqualTo(0);
    }
}
