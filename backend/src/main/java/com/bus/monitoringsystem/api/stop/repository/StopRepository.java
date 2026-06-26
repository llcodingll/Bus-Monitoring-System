package com.bus.monitoringsystem.api.stop.repository;

import com.bus.monitoringsystem.api.stop.model.Stop;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StopRepository extends JpaRepository<Stop, Long> {
}
