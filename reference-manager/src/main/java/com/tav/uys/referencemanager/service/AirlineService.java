package com.tav.uys.referencemanager.service;

import com.tav.uys.referencemanager.entity.Airline;
import com.tav.uys.referencemanager.repository.AirlineRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AirlineService {

    private final AirlineRepository airlineRepository;

    public AirlineService(AirlineRepository airlineRepository) {
        this.airlineRepository = airlineRepository;
    }

    public Airline createAirline(Airline airline) {
        return airlineRepository.save(airline);
    }

@Cacheable(value = "airlines")
    public List<Airline> getAllAirlines() {
        return airlineRepository.findAll();
    }
}