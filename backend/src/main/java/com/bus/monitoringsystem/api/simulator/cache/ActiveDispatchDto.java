package com.bus.monitoringsystem.api.simulator.cache;

import com.bus.monitoringsystem.api.routestop.model.Direction;

public record ActiveDispatchDto(Long dispatchId, Long busId, String busNumber, Long routeId, Direction direction) {
}
