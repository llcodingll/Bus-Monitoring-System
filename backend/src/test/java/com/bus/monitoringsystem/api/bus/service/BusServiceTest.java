package com.bus.monitoringsystem.api.bus.service;

import com.bus.monitoringsystem.api.bus.dto.response.BusDetailResponse;
import com.bus.monitoringsystem.api.bus.model.Bus;
import com.bus.monitoringsystem.api.bus.model.BusStatus;
import com.bus.monitoringsystem.api.bus.policy.OnlineStatusPolicy;
import com.bus.monitoringsystem.api.bus.repository.BusRepository;
import com.bus.monitoringsystem.api.dispatch.model.BusDispatch;
import com.bus.monitoringsystem.api.dispatch.repository.BusDispatchRepository;
import com.bus.monitoringsystem.api.route.model.Route;
import com.bus.monitoringsystem.api.routestop.model.Direction;
import com.bus.monitoringsystem.api.stop.model.Stop;
import com.bus.monitoringsystem.common.BaseException;
import com.bus.monitoringsystem.common.BaseResponseStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("BusService")
class BusServiceTest {

    @InjectMocks
    private BusService busService;

    @Mock
    private BusRepository busRepository;

    @Mock
    private BusDispatchRepository busDispatchRepository;

    @Mock
    private OnlineStatusPolicy onlineStatusPolicy;

    @Test
    @DisplayName("유효한 버스 ID로 상세 조회하면 BusDetailResponse를 반환한다")
    void findDetail_returnsResponse_whenBusExists() {

        // given
        Stop currentStop = mock(Stop.class);
        given(currentStop.getStopName()).willReturn("강남역");

        Stop nextStop = mock(Stop.class);
        given(nextStop.getStopName()).willReturn("역삼역");

        Bus bus = mock(Bus.class);
        given(bus.getId()).willReturn(1L);
        given(bus.getBusNumber()).willReturn("143-1");
        given(bus.getCurrentSpeed()).willReturn(40);
        given(bus.getLastCommunicationAt()).willReturn(LocalDateTime.now());
        given(bus.getCurrentStop()).willReturn(currentStop);
        given(bus.getNextStop()).willReturn(nextStop);
        given(bus.getCurrentLatitude()).willReturn(new BigDecimal("37.5000000"));
        given(bus.getCurrentLongitude()).willReturn(new BigDecimal("127.0000000"));

        Route route = mock(Route.class);
        given(route.getRouteNumber()).willReturn("143");
        given(route.getRouteName()).willReturn("143번");

        BusDispatch dispatch = mock(BusDispatch.class);
        given(dispatch.getBus()).willReturn(bus);
        given(dispatch.getRoute()).willReturn(route);
        given(dispatch.getDirection()).willReturn(Direction.OUTBOUND);
        given(dispatch.getOperationStartedAt()).willReturn(LocalDateTime.now().minusHours(1));

        given(busRepository.findByIdWithStops(1L)).willReturn(Optional.of(bus));
        given(busDispatchRepository.findAllActiveWithRoute()).willReturn(List.of(dispatch));
        given(onlineStatusPolicy.resolve(any(), any())).willReturn(BusStatus.ONLINE);

        // when
        BusDetailResponse result = busService.findDetail(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getBusNumber()).isEqualTo("143-1");
        assertThat(result.getRouteName()).isEqualTo("143번");
        assertThat(result.getCurrentStopName()).isEqualTo("강남역");
        assertThat(result.getNextStopName()).isEqualTo("역삼역");
        assertThat(result.getDirection()).isEqualTo("OUTBOUND");
        then(busRepository).should().findByIdWithStops(1L);
    }

    @Test
    @DisplayName("존재하지 않는 버스 ID로 조회하면 BUS_NOT_FOUND 예외가 발생한다")
    void findDetail_throwsException_whenBusNotFound() {

        // given
        given(busRepository.findByIdWithStops(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> busService.findDetail(999L))
                .isInstanceOf(BaseException.class)
                .hasMessageContaining(BaseResponseStatus.BUS_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("배차 정보가 없는 버스도 정상 조회된다")
    void findDetail_returnsResponse_whenNoDispatch() {

        // given
        Bus bus = mock(Bus.class);
        given(bus.getId()).willReturn(2L);
        given(bus.getBusNumber()).willReturn("999-1");
        given(bus.getCurrentSpeed()).willReturn(0);
        given(bus.getLastCommunicationAt()).willReturn(null);
        given(bus.getCurrentStop()).willReturn(null);
        given(bus.getNextStop()).willReturn(null);
        given(bus.getCurrentLatitude()).willReturn(null);
        given(bus.getCurrentLongitude()).willReturn(null);

        given(busRepository.findByIdWithStops(2L)).willReturn(Optional.of(bus));
        given(busDispatchRepository.findAllActiveWithRoute()).willReturn(List.of());
        given(onlineStatusPolicy.resolve(any(), any())).willReturn(BusStatus.OFFLINE);

        // when
        BusDetailResponse result = busService.findDetail(2L);

        // then
        assertThat(result.getRouteName()).isNull();
        assertThat(result.getDirection()).isNull();
        assertThat(result.getOperationStartedAt()).isNull();
    }
}
