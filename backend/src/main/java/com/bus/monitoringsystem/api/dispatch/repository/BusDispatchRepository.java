package com.bus.monitoringsystem.api.dispatch.repository;

import com.bus.monitoringsystem.api.dispatch.model.BusDispatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BusDispatchRepository extends JpaRepository<BusDispatch, Long> {

    @Query("SELECT d FROM BusDispatch d JOIN FETCH d.route JOIN FETCH d.bus WHERE d.operationEndedAt IS NULL")
    List<BusDispatch> findAllActiveWithRoute();
}
