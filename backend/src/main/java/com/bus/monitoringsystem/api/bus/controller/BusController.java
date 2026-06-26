package com.bus.monitoringsystem.api.bus.controller;

import com.bus.monitoringsystem.api.bus.dto.response.BusDetailResponse;
import com.bus.monitoringsystem.api.bus.dto.response.BusPathPointResponse;
import com.bus.monitoringsystem.api.bus.dto.response.BusSummaryResponse;
import com.bus.monitoringsystem.api.bus.service.BusService;
import com.bus.monitoringsystem.api.event.dto.response.EventSummaryResponse;
import com.bus.monitoringsystem.api.event.service.EventService;
import com.bus.monitoringsystem.common.BaseResponse;
import com.bus.monitoringsystem.common.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buses")
@RequiredArgsConstructor
public class BusController {

    private final BusService busService;
    private final EventService eventService;

    @GetMapping
    public ResponseEntity<BaseResponse<List<BusSummaryResponse>>> findAllBusSummaries() {

        List<BusSummaryResponse> result = busService.findAllBusSummaries();
        return ResponseEntity.ok(BaseResponse.success(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<BusDetailResponse>> findBusDetail(@PathVariable Long id) {

        BusDetailResponse result = busService.findDetail(id);
        return ResponseEntity.ok(BaseResponse.success(result));
    }

    @GetMapping("/{id}/path")
    public ResponseEntity<BaseResponse<List<BusPathPointResponse>>> findBusPath(@PathVariable Long id) {

        List<BusPathPointResponse> result = busService.findBusPath(id);
        return ResponseEntity.ok(BaseResponse.success(result));
    }

    @GetMapping("/{id}/events")
    public ResponseEntity<BaseResponse<PageResponse<EventSummaryResponse>>> findBusEvents(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageResponse<EventSummaryResponse> result = eventService.findRecentEventsByBusId(id, page, size);
        return ResponseEntity.ok(BaseResponse.success(result));
    }
}
