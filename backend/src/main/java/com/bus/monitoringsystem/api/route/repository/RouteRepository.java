package com.bus.monitoringsystem.api.route.repository;

import com.bus.monitoringsystem.api.route.model.Route;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RouteRepository extends JpaRepository<Route, Long> {
}
