package com.bus.monitoringsystem.api.simulator.cache;

import com.bus.monitoringsystem.api.dispatch.repository.BusDispatchRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ActiveDispatchCache {

    private final BusDispatchRepository busDispatchRepository;

    private List<ActiveDispatchDto> dispatches = List.of();

    @PostConstruct
    public void load() {

        dispatches = busDispatchRepository.findAllActiveWithBusAndStops().stream()
                .map(d -> new ActiveDispatchDto(
                        d.getId(),
                        d.getBus().getId(),
                        d.getBus().getBusNumber(),
                        d.getRoute().getId(),
                        d.getDirection()))
                .toList();
    }

    public List<ActiveDispatchDto> getAll() {

        return dispatches;
    }
}
