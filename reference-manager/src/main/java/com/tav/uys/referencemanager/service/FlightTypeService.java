package com.tav.uys.referencemanager.service;

import com.tav.uys.referencemanager.entity.FlightType;
import com.tav.uys.referencemanager.repository.FlightTypeRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class FlightTypeService {
    private final FlightTypeRepository flightTypeRepository;
    public FlightTypeService(FlightTypeRepository flightTypeRepository) {
        this.flightTypeRepository = flightTypeRepository;
    }
    public FlightType createFlightType(FlightType flightType) {
        return flightTypeRepository.save(flightType);
    }
    public List<FlightType> getAllFlightTypes() {
        return flightTypeRepository.findAll();
    }
    public Optional<FlightType> getFlightTypeById(Long id) {
        return flightTypeRepository.findById(id);
    }
    public void deleteFlightType(Long id) {
        flightTypeRepository.deleteById(id);
    }
}
