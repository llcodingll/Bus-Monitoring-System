package com.bus.monitoringsystem.api.routestop.repository;

import com.bus.monitoringsystem.api.routestop.model.Direction;
import com.bus.monitoringsystem.api.routestop.model.RouteStop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RouteStopRepository extends JpaRepository<RouteStop, Long> {

    @Query("SELECT rs FROM RouteStop rs JOIN FETCH rs.stop WHERE rs.route.id = :routeId AND rs.direction = :direction AND rs.stopOrder = :stopOrder")
    Optional<RouteStop> findWithStopByRouteIdAndDirectionAndStopOrder(Long routeId, Direction direction, int stopOrder);

    @Query("SELECT MAX(rs.stopOrder) FROM RouteStop rs WHERE rs.route.id = :routeId AND rs.direction = :direction")
    Optional<Integer> findMaxStopOrderByRouteIdAndDirection(Long routeId, Direction direction);

    @Query("SELECT rs.stopOrder FROM RouteStop rs WHERE rs.route.id = :routeId AND rs.stop.id = :stopId AND rs.direction = :direction")
    Optional<Integer> findStopOrderByRouteIdAndStopIdAndDirection(Long routeId, Long stopId, Direction direction);
}
