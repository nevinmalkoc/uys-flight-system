package com.tav.uys.flight_service.dto;

import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;

public record FlightUpdateRequestDto(
        LocalDateTime departureTime,

        @Pattern(regexp = "^(SCHEDULED|DELAYED|BOARDING|DEPARTED|ARRIVED|CANCELLED)$",
                message = "Gecersiz ucus statusu! Kabul edilenler: SCHEDULED, DELAYED, BOARDING, DEPARTED, ARRIVED, CANCELLED")
        String status
) {}