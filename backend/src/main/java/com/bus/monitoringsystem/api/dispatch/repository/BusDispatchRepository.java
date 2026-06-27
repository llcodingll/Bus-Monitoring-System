package com.bus.monitoringsystem.api.dispatch.repository;

import com.bus.monitoringsystem.api.dispatch.model.BusDispatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BusDispatchRepository extends JpaRepository<BusDispatch, Long> {

    @Query("SELECT d FROM BusDispatch d JOIN FETCH d.route WHERE d.operationEndedAt IS NULL")
    List<BusDispatch> findAllActiveWithRoute();

    @Query("""
            SELECT d FROM BusDispatch d
            JOIN FETCH d.route
            WHERE d.bus.id = :busId
              AND d.operationEndedAt IS NULL
            """)
    Optional<BusDispatch> findActiveByBusId(Long busId);

    @Query("""
            SELECT d FROM BusDispatch d
            JOIN FETCH d.route
            JOIN FETCH d.bus b
            LEFT JOIN FETCH b.currentStop
            LEFT JOIN FETCH b.nextStop
            WHERE d.operationEndedAt IS NULL
            """)
    List<BusDispatch> findAllActiveWithBusAndStops();
}
