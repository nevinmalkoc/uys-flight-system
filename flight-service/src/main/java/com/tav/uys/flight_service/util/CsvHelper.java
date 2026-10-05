package com.tav.uys.flight_service.util;

import com.tav.uys.flight_service.entity.Flight;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CsvHelper {

    public static List<Flight> csvToFlights(MultipartFile file) {
        List<Flight> flights = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                // Başlık satırını atla
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] values = line.split(",");
                if (values.length >= 4) {
                    Flight flight = new Flight();
                    flight.setFlightNumber(values[0].trim());
                    flight.setDepartureStation(values[1].trim());
                    flight.setArrivalStation(values[2].trim());
                    flight.setDepartureTime(LocalDateTime.parse(values[3].trim()));
                    flights.add(flight);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("CSV dosyasi okunurken hata olustu: " + e.getMessage());
        }

        return flights;
    }
}