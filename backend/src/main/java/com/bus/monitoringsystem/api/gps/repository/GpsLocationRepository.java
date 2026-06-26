package com.bus.monitoringsystem.api.gps.repository;

import com.bus.monitoringsystem.api.gps.model.GpsLocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GpsLocationRepository extends JpaRepository<GpsLocation, Long> {
}
