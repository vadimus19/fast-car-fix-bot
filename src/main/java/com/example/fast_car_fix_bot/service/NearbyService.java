package com.example.fast_car_fix_bot.service;

import com.example.fast_car_fix_bot.bot.util.GeoUtils;
import com.example.fast_car_fix_bot.entity.ServiceCenter;
import com.example.fast_car_fix_bot.repository.ServiceCenterRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class NearbyService {

    private final ServiceCenterRepository repository;

    public NearbyService(ServiceCenterRepository repository) {
        this.repository = repository;
    }

    public List<ServiceCenter> findNearest(double lat, double lon) {

        List<ServiceCenter> centers = repository.findNearby(lat, lon);

        return centers.stream()
                .sorted(Comparator.comparingDouble(
                        c -> GeoUtils.distance(
                                lat,
                                lon,
                                c.getLocation().getY(),
                                c.getLocation().getX()
                        )
                ))
                .limit(5)
                .toList();
    }
}
