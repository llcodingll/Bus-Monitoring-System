package com.bus.monitoringsystem.api.event.controller;

import com.bus.monitoringsystem.api.event.dto.response.EventSummaryResponse;
import com.bus.monitoringsystem.api.event.service.EventService;
import com.bus.monitoringsystem.common.BaseResponse;
import com.bus.monitoringsystem.common.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<BaseResponse<PageResponse<EventSummaryResponse>>> findRecentEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageResponse<EventSummaryResponse> result = eventService.findRecentEvents(page, size);
        return ResponseEntity.ok(BaseResponse.success(result));
    }
}
