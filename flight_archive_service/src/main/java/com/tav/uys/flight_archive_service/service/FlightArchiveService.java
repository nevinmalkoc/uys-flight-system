package com.tav.uys.flight_archive_service.service;

import com.tav.uys.flight_archive_service.entity.FlightArchive;
import com.tav.uys.flight_archive_service.repository.FlightArchiveRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class FlightArchiveService {

    private final FlightArchiveRepository archiveRepository;

    public FlightArchiveService(FlightArchiveRepository archiveRepository) {
        this.archiveRepository = archiveRepository;
    }

    public List<FlightArchive> getAllArchives() {
        return archiveRepository.findAll();
    }

    public List<FlightArchive> getFlightHistory(String flightNumber) {
        return archiveRepository.findByFlightNumberOrderByVersionAsc(flightNumber);
    }

    // Geciken uçuşları getirir
    public List<FlightArchive> getDelayedFlights() {
        return archiveRepository.findByStatus("DELAYED");
    }

    // Statü bazlı operasyonel istatistikleri getirir
    public List<Map<String, Object>> getStatusStatistics() {
        return archiveRepository.countFlightsByStatus();
    }
}