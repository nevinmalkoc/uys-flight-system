package com.tav.uys.referencemanager.service;

import com.tav.uys.referencemanager.entity.Aircraft;
import com.tav.uys.referencemanager.repository.AircraftRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AircraftService {

    private final AircraftRepository aircraftRepository;

    public AircraftService(AircraftRepository aircraftRepository) {
        this.aircraftRepository = aircraftRepository;
    }

    public Aircraft createAircraft(Aircraft aircraft) {
        return aircraftRepository.save(aircraft);
    }

    @Cacheable(value = "aircrafts")
    public List<Aircraft> getAllAircrafts() {
        return aircraftRepository.findAll();
    }
}