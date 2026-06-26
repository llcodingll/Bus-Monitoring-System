package com.bus.monitoringsystem.api.bus.controller;

import com.bus.monitoringsystem.api.bus.dto.response.BusSummaryResponse;
import com.bus.monitoringsystem.api.bus.service.BusService;
import com.bus.monitoringsystem.common.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/buses")
@RequiredArgsConstructor
public class BusController {

    private final BusService busService;

    @GetMapping
    public ResponseEntity<BaseResponse<List<BusSummaryResponse>>> findAllBusSummaries() {

        List<BusSummaryResponse> result = busService.findAllBusSummaries();
        return ResponseEntity.ok(BaseResponse.success(result));
    }
}
