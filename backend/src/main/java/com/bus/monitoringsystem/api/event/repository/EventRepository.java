package com.bus.monitoringsystem.api.event.repository;

import com.bus.monitoringsystem.api.event.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT e FROM Event e JOIN FETCH e.bus JOIN FETCH e.route ORDER BY e.occurredAt DESC")
    Page<Event> findAllWithBusAndRoute(Pageable pageable);

    @Query("SELECT e FROM Event e JOIN FETCH e.bus JOIN FETCH e.route WHERE e.bus.id = :busId ORDER BY e.occurredAt DESC")
    Page<Event> findAllByBusIdWithBusAndRoute(Long busId, Pageable pageable);
}
