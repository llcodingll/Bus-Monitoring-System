package com.bus.monitoringsystem.api.routestop.repository;

import com.bus.monitoringsystem.api.routestop.model.RouteStop;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RouteStopRepository extends JpaRepository<RouteStop, Long> {
}
