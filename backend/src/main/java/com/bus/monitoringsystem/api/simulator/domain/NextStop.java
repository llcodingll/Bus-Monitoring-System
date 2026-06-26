package com.bus.monitoringsystem.api.simulator.domain;

import com.bus.monitoringsystem.api.routestop.model.Direction;

public record NextStop(int stopOrder, Direction direction) {
}
