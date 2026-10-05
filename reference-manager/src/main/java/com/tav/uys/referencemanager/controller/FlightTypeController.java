package com.tav.uys.referencemanager.controller;

import com.tav.uys.referencemanager.entity.FlightType;
import com.tav.uys.referencemanager.service.FlightTypeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/flight-types")
public class FlightTypeController {
    private final FlightTypeService flightTypeService;
    public FlightTypeController(FlightTypeService flightTypeService) {
        this.flightTypeService = flightTypeService;
    }
    @PostMapping
    public FlightType createFlightType(@RequestBody FlightType flightType) {
        return flightTypeService.createFlightType(flightType);
    }
    @GetMapping
    public List<FlightType> getAllFlightTypes() {
        return flightTypeService.getAllFlightTypes();
    }
    @GetMapping("/{id}")
    public ResponseEntity<FlightType> getFlightTypeById(@PathVariable Long id) {
        return flightTypeService.getFlightTypeById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFlightType(@PathVariable Long id) {
        flightTypeService.deleteFlightType(id);
        return ResponseEntity.noContent().build();
    }
}
