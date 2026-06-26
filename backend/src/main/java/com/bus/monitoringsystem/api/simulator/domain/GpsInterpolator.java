package com.bus.monitoringsystem.api.simulator.domain;

import org.springframework.stereotype.Component;

@Component
public class GpsInterpolator {

    public double[] interpolate(double fromLat, double fromLng,
                                double toLat, double toLng,
                                double progress) {

        double clamped = Math.max(0.0, Math.min(1.0, progress));
        return new double[]{
                fromLat + (toLat - fromLat) * clamped,
                fromLng + (toLng - fromLng) * clamped
        };
    }
}
