package com.tav.uys.flight_service.dto;

import jakarta.validation.constraints.AssertFalse;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;

public record FlightCreateRequestDto(
        @NotBlank(message = "Ucus numarasi bos olamaz")
        @Pattern(regexp = "^[A-Z0-9]{2}[0-9]{1,4}[A-Z]?$", message = "Gecersiz ucus numarasi formati! Ornek: TK2026, PC123")
        String flightNumber,

        @NotBlank(message = "Kalkis istasyonu bos olamaz")
        @Pattern(regexp = "^[A-Z]{3,4}$", message = "Kalkis istasyonu 3 veya 4 buyuk harfli IATA/ICAO kodu olmalidir (Orn: IST, LTFM)")
        String departureStation,

        @NotBlank(message = "Varis istasyonu bos olamaz")
        @Pattern(regexp = "^[A-Z]{3,4}$", message = "Varis istasyonu 3 veya 4 buyuk harfli IATA/ICAO kodu olmalidir (Orn: ADB, LTBA)")
        String arrivalStation,

        @NotNull(message = "Kalkis zamani bos olamaz")
        @FutureOrPresent(message = "Kalkis zamani gecmis bir tarih olamaz")
        LocalDateTime departureTime
) {
    // Dokumandaki "Origin != Destination" kurali
    @AssertFalse(message = "Kalkis ve varis istasyonlari ayni olamaz!")
    public boolean isSameStation() {
        return departureStation != null && departureStation.equalsIgnoreCase(arrivalStation);
    }
}