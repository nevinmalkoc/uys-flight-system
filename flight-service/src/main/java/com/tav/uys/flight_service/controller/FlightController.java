package com.tav.uys.flight_service.controller;

import com.tav.uys.flight_service.dto.FlightCreateRequestDto;
import com.tav.uys.flight_service.dto.FlightResponseDto;
import com.tav.uys.flight_service.dto.FlightUpdateRequestDto;
import com.tav.uys.flight_service.entity.Flight;
import com.tav.uys.flight_service.service.FlightService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/flights")
public class FlightController {

    private final FlightService flightService;

    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    @PostMapping
    public ResponseEntity<FlightResponseDto> createFlight(@Valid @RequestBody FlightCreateRequestDto requestDto) {
        return ResponseEntity.ok(flightService.createFlight(requestDto));
    }

    @GetMapping
    public ResponseEntity<List<FlightResponseDto>> getAllFlights() {
        return ResponseEntity.ok(flightService.getAllFlights());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FlightResponseDto> getFlightById(@PathVariable Long id) {
        return ResponseEntity.ok(flightService.getFlightById(id));
    }

    @PostMapping(value = "/upload-csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadCsv(@RequestParam("file") MultipartFile file) {
        List<Flight> saved = flightService.saveAllFromCsv(file);
        return ResponseEntity.ok("CSV basariyla islendi. Eklenen ucus sayisi: " + saved.size());
    }

    @PutMapping("/{flightNumber}")
    public ResponseEntity<FlightResponseDto> updateFlight(
            @PathVariable String flightNumber,
            @Valid @RequestBody FlightUpdateRequestDto requestDto) {
        return ResponseEntity.ok(flightService.updateFlight(flightNumber, requestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteFlight(@PathVariable Long id) {
        flightService.deleteFlight(id);
        return ResponseEntity.ok("Ucus basariyla iptal edildi ve pasife alindi (ID: " + id + ")");
    }
}