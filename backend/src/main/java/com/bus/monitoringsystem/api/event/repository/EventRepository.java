package com.bus.monitoringsystem.api.event.repository;

import com.bus.monitoringsystem.api.event.model.Event;
import com.bus.monitoringsystem.api.event.model.EventType;
import com.bus.monitoringsystem.api.event.model.Severity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("""
            SELECT e FROM Event e JOIN FETCH e.bus JOIN FETCH e.route
            WHERE (:eventType IS NULL OR e.eventType = :eventType)
              AND (:severity  IS NULL OR e.severity  = :severity)
            ORDER BY e.occurredAt DESC
            """)
    Page<Event> findAllWithFilters(EventType eventType, Severity severity, Pageable pageable);

    @Query("""
            SELECT e FROM Event e JOIN FETCH e.bus JOIN FETCH e.route
            WHERE e.bus.id = :busId
              AND (:eventType IS NULL OR e.eventType = :eventType)
              AND (:severity  IS NULL OR e.severity  = :severity)
            ORDER BY e.occurredAt DESC
            """)
    Page<Event> findAllByBusIdWithFilters(Long busId, EventType eventType, Severity severity, Pageable pageable);
}
