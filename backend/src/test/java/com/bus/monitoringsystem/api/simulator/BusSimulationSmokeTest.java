package com.bus.monitoringsystem.api.simulator;

import com.bus.monitoringsystem.api.routestop.model.Direction;
import com.bus.monitoringsystem.api.simulator.cache.ActiveDispatchCache;
import com.bus.monitoringsystem.api.simulator.cache.ActiveDispatchDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("BusSimulationScheduler")
class BusSimulationSmokeTest {

    @Mock private ActiveDispatchCache activeDispatchCache;
    @Mock private BusTickProcessor busTickProcessor;

    private BusSimulationScheduler scheduler;

    @BeforeEach
    void setUp() {

        scheduler = new BusSimulationScheduler(activeDispatchCache, busTickProcessor);
    }

    @Test
    @DisplayName("1 tick 실행 후 온라인 버스에 대해 BusTickProcessor가 호출된다")
    void tick_callsProcessor_forOnlineBus() {

        // given
        ActiveDispatchDto dto = new ActiveDispatchDto(1L, 1L, "서울71가1234", 1L, Direction.OUTBOUND);
        BusSimState returnedState = new BusSimState(1, Direction.OUTBOUND, 0.18, 30.0);
        given(activeDispatchCache.getAll()).willReturn(List.of(dto));
        given(busTickProcessor.process(dto, null)).willReturn(returnedState);

        // when
        scheduler.tick();

        // then
        then(busTickProcessor).should().process(dto, null);
    }

    @Test
    @DisplayName("OFFLINE 지정 버스는 tick에서 건너뛰어 BusTickProcessor가 호출되지 않는다")
    void tick_skipsOfflineBus_withoutCallingProcessor() {

        // given
        ActiveDispatchDto dto = new ActiveDispatchDto(1L, 1L, "서울75바3456", 1L, Direction.OUTBOUND);
        given(activeDispatchCache.getAll()).willReturn(List.of(dto));

        // when
        scheduler.tick();

        // then
        then(busTickProcessor).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("2번째 tick에서는 첫 번째 tick의 반환 상태를 BusTickProcessor에 전달한다")
    void tick_passesStateFromPreviousTick_onSecondTick() {

        // given
        ActiveDispatchDto dto = new ActiveDispatchDto(1L, 1L, "서울71가1234", 1L, Direction.OUTBOUND);
        BusSimState firstState = new BusSimState(1, Direction.OUTBOUND, 0.18, 30.0);
        BusSimState secondState = new BusSimState(1, Direction.OUTBOUND, 0.36, 35.0);
        given(activeDispatchCache.getAll()).willReturn(List.of(dto));
        given(busTickProcessor.process(dto, null)).willReturn(firstState);
        given(busTickProcessor.process(dto, firstState)).willReturn(secondState);

        // when
        scheduler.tick();
        scheduler.tick();

        // then
        then(busTickProcessor).should().process(dto, null);
        then(busTickProcessor).should().process(dto, firstState);
    }
}
