package com.bus.monitoringsystem.api.seed.controller;

import com.bus.monitoringsystem.api.seed.dto.response.SeedResponse;
import com.bus.monitoringsystem.api.seed.dto.result.SeedResult;
import com.bus.monitoringsystem.api.seed.service.SeedService;
import com.bus.monitoringsystem.common.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seed")
@RequiredArgsConstructor
public class SeedController {

    private final SeedService seedService;

    @PostMapping
    public ResponseEntity<BaseResponse<SeedResponse>> seed() {

        SeedResult result = seedService.seed();
        return ResponseEntity.ok(BaseResponse.success(SeedResponse.from(result)));
    }
}
