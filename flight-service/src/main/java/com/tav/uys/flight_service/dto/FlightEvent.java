package com.tav.uys.flight_service.dto;

import com.tav.uys.flight_service.entity.Flight;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightEvent {
    private String eventType; // FLIGHT_CREATED veya FLIGHT_UPDATED
    private String flightNumber;
    private Flight payload;
    private LocalDateTime timestamp;
}
