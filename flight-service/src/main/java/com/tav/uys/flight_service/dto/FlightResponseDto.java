//database e kaydedilecek uçuşun istemciye dönülecek biçimlendiirlmiş özeti sunar
package com.tav.uys.flight_service.dto;

import java.time.LocalDateTime;

public record FlightResponseDto(
        Long id,
        String flightNumber,
        String departureStation,
        String arrivalStation,
        LocalDateTime departureTime,
        Integer version,
        String status,
        Boolean isCurrent
) {
}