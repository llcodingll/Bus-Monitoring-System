package com.bus.monitoringsystem.api.bus.repository;

import com.bus.monitoringsystem.api.bus.model.Bus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BusRepository extends JpaRepository<Bus, Long> {

    @Query("SELECT b FROM Bus b LEFT JOIN FETCH b.currentStop LEFT JOIN FETCH b.nextStop")
    List<Bus> findAllWithStops();

    @Query("SELECT b FROM Bus b LEFT JOIN FETCH b.currentStop LEFT JOIN FETCH b.nextStop WHERE b.id = :id")
    Optional<Bus> findByIdWithStops(Long id);
}
