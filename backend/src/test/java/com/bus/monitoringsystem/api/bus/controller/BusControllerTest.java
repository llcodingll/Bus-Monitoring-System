package com.bus.monitoringsystem.api.bus.controller;

import com.bus.monitoringsystem.api.bus.dto.response.BusDetailResponse;
import com.bus.monitoringsystem.api.bus.dto.response.BusPathPointResponse;
import com.bus.monitoringsystem.api.bus.dto.response.BusSummaryResponse;
import com.bus.monitoringsystem.api.bus.service.BusService;
import com.bus.monitoringsystem.api.event.dto.response.EventSummaryResponse;
import com.bus.monitoringsystem.api.event.service.EventService;
import com.bus.monitoringsystem.common.BaseException;
import com.bus.monitoringsystem.common.BaseResponse;
import com.bus.monitoringsystem.common.BaseResponseStatus;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("BusController")
class BusControllerTest {

    @InjectMocks
    private BusController busController;

    @Mock
    private BusService busService;

    @Mock
    private EventService eventService;

    @Test
    @DisplayName("유효한 버스 ID 조회 시 200과 BusDetailResponse를 반환한다")
    void findBusDetail_returns200_whenBusExists() {

        // given
        BusDetailResponse mockResponse = BusDetailResponse.builder()
                .id(1L)
                .busNumber("143-1")
                .routeNumber("143")
                .routeName("143번")
                .currentSpeed(40)
                .status("ONLINE")
                .lastCommunicationAt("2026-06-26T10:00:00")
                .currentStopName("강남역")
                .nextStopName("역삼역")
                .direction("OUTBOUND")
                .operationStartedAt("2026-06-26T08:00:00")
                .currentLatitude(new BigDecimal("37.5000000"))
                .currentLongitude(new BigDecimal("127.0000000"))
                .build();

        given(busService.findDetail(1L)).willReturn(mockResponse);

        // when
        ResponseEntity<BaseResponse<BusDetailResponse>> result = busController.findBusDetail(1L);

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getResult().getBusNumber()).isEqualTo("143-1");
        assertThat(result.getBody().getResult().getStatus()).isEqualTo("ONLINE");
        then(busService).should().findDetail(1L);
    }

    @Test
    @DisplayName("존재하지 않는 버스 ID 조회 시 BUS_NOT_FOUND 예외가 전파된다")
    void findBusDetail_throwsException_whenBusNotFound() {

        // given
        given(busService.findDetail(999L)).willThrow(new BaseException(BaseResponseStatus.BUS_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> busController.findBusDetail(999L))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining(BaseResponseStatus.BUS_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("GET /api/buses - 전체 버스 목록을 200으로 반환한다")
    void findAllBusSummaries_returns200WithBusList() {

        // given
        given(busService.findAllBusSummaries()).willReturn(List.of());

        // when
        ResponseEntity<BaseResponse<List<BusSummaryResponse>>> result = busController.findAllBusSummaries();

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        then(busService).should().findAllBusSummaries();
    }

    @Test
    @DisplayName("GET /api/buses/{id}/path - 버스 GPS 경로를 200으로 반환한다")
    void findBusPath_returns200WithPathList() {

        // given
        given(busService.findBusPath(1L)).willReturn(List.of());

        // when
        ResponseEntity<BaseResponse<List<BusPathPointResponse>>> result = busController.findBusPath(1L);

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        then(busService).should().findBusPath(1L);
    }

    @Test
    @DisplayName("GET /api/buses/{id}/events - 버스 이벤트 목록을 200으로 반환한다")
    void findBusEvents_returns200WithPagedEvents() {

        // given
        PageResponse<EventSummaryResponse> pageResponse = PageResponse.<EventSummaryResponse>builder()
                .content(List.of())
                .totalElements(0)
                .totalPages(0)
                .currentPage(0)
                .size(10)
                .build();

        given(eventService.findRecentEventsByBusId(1L, 0, 10)).willReturn(pageResponse);

        // when
        ResponseEntity<BaseResponse<PageResponse<EventSummaryResponse>>> result = busController.findBusEvents(1L, 0, 10);

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        then(eventService).should().findRecentEventsByBusId(1L, 0, 10);
    }
}
