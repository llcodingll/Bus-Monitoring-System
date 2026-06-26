package com.bus.monitoringsystem.api.event.repository;

import com.bus.monitoringsystem.api.event.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
}
