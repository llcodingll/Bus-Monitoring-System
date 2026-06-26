package com.bus.monitoringsystem.api.simulator;

import com.bus.monitoringsystem.api.routestop.model.Direction;

record BusSimState(int currentStopOrder, Direction direction, double progress, Double prevSpeedKmh) {
}
