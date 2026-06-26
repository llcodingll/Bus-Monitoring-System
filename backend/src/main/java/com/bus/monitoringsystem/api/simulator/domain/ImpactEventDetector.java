package com.bus.monitoringsystem.api.simulator.domain;

import org.springframework.stereotype.Component;

@Component
public class ImpactEventDetector {

    private static final double IMPACT_THRESHOLD_G = 0.5;

    public boolean isImpact(double magnitudeG) {

        return Math.abs(magnitudeG) >= IMPACT_THRESHOLD_G;
    }
}
