package com.tav.uys.flight_archive_service.controller;

import com.tav.uys.flight_archive_service.entity.FlightArchive;
import com.tav.uys.flight_archive_service.repository.FlightArchiveRepository;
import com.tav.uys.flight_archive_service.service.FlightArchiveService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/archives")
public class FlightArchiveController {

    private final FlightArchiveRepository archiveRepository;
    private final FlightArchiveService archiveService;

    public FlightArchiveController(FlightArchiveRepository archiveRepository, FlightArchiveService archiveService) {
        this.archiveRepository = archiveRepository;
        this.archiveService = archiveService;
    }

    @GetMapping
    public ResponseEntity<Page<FlightArchive>> getAllArchives(
            @RequestParam(required = false) String flightNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        if (flightNumber != null && !flightNumber.trim().isEmpty()) {
            return ResponseEntity.ok(archiveRepository.findByFlightNumber(flightNumber.trim(), pageable));
        }

        return ResponseEntity.ok(archiveRepository.findAll(pageable));
    }

    @GetMapping("/history/{flightNumber}")
    public ResponseEntity<List<FlightArchive>> getFlightHistory(@PathVariable String flightNumber) {
        return ResponseEntity.ok(archiveService.getFlightHistory(flightNumber.trim()));
    }

    // Rapor: Geciken uçuşlar
    @GetMapping("/reports/delayed")
    public ResponseEntity<List<FlightArchive>> getDelayedFlights() {
        return ResponseEntity.ok(archiveService.getDelayedFlights());
    }

    // Rapor/KPI: Statü dağılımı
    @GetMapping("/reports/stats")
    public ResponseEntity<List<Map<String, Object>>> getStatusStatistics() {
        return ResponseEntity.ok(archiveService.getStatusStatistics());
    }
}