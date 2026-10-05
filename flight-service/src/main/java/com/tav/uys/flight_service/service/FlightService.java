package com.tav.uys.flight_service.service;

import com.tav.uys.flight_service.client.ReferenceClient;
import com.tav.uys.flight_service.dto.*;
import com.tav.uys.flight_service.entity.Flight;
import com.tav.uys.flight_service.repository.FlightRepository;
import com.tav.uys.flight_service.util.CsvHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class FlightService {

    private final FlightRepository flightRepository;
    private final ReferenceClient referenceClient;
    private final ReferenceCacheService cacheService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    public FlightService(FlightRepository flightRepository,
                         ReferenceClient referenceClient,
                         ReferenceCacheService cacheService,
                         KafkaTemplate<String, Object> kafkaTemplate,
                         SimpMessagingTemplate messagingTemplate) {
        this.flightRepository = flightRepository;
        this.referenceClient = referenceClient;
        this.cacheService = cacheService;
        this.kafkaTemplate = kafkaTemplate;
        this.messagingTemplate = messagingTemplate;
    }

    public FlightResponseDto createFlight(FlightCreateRequestDto requestDto) {
        validateStation(requestDto.departureStation());
        validateStation(requestDto.arrivalStation());

        Flight flight = new Flight();
        flight.setFlightNumber(requestDto.flightNumber());
        flight.setDepartureStation(requestDto.departureStation());
        flight.setArrivalStation(requestDto.arrivalStation());
        flight.setDepartureTime(requestDto.departureTime());

        Flight savedFlight = flightRepository.save(flight);

        // FAZ 4: Standart FLIGHT_CREATED eventi basılıyor
        FlightEvent event = FlightEvent.builder()
                .eventType("FLIGHT_CREATED")
                .flightNumber(savedFlight.getFlightNumber())
                .payload(savedFlight)
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send("flight.events", savedFlight.getFlightNumber(), event);

        FlightResponseDto responseDto = mapToResponseDto(savedFlight);
        messagingTemplate.convertAndSend("/topic/flights", responseDto);
        return responseDto;
    }

    private void validateStation(String stationCode) {
        if (stationCode == null || stationCode.isBlank()) {
            throw new IllegalArgumentException("İstasyon kodu boş olamaz!");
        }

        boolean existsInCache = cacheService.exists("STATION", stationCode);
        if (existsInCache) {
            log.info("[CACHE HIT] İstasyon Redis'te doğrulandı: {}", stationCode);
            return;
        }

        log.warn("[CACHE MISS] İstasyon Redis'te bulunamadı, Reference Service'e soruluyor: {}", stationCode);
        List<StationDto> stations = referenceClient.getAllStations();
        boolean existsInService = stations != null && stations.stream()
                .anyMatch(s -> s.getIataCode().equalsIgnoreCase(stationCode));

        if (!existsInService) {
            throw new IllegalArgumentException("Geçersiz istasyon! '" + stationCode + "' sistemde kayıtlı değil.");
        }

        cacheService.put("STATION", stationCode, "VALID");
    }

    public List<FlightResponseDto> getAllFlights() {
        return flightRepository.findByIsCurrentTrue().stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    public FlightResponseDto getFlightById(Long id) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Uçuş bulunamadı, ID: " + id));
        return mapToResponseDto(flight);
    }

    @Transactional
    public List<Flight> saveAllFromCsv(MultipartFile file) {
        List<Flight> flights = CsvHelper.csvToFlights(file);

        if (flights == null || flights.isEmpty()) {
            throw new IllegalArgumentException("CSV dosyası boş veya geçerli veri bulunamadı!");
        }

        for (Flight f : flights) {
            // Görünmeyen karakter ve boşlukları temizle
            if (f.getDepartureStation() != null) {
                f.setDepartureStation(f.getDepartureStation().trim().toUpperCase());
            }
            if (f.getArrivalStation() != null) {
                f.setArrivalStation(f.getArrivalStation().trim().toUpperCase());
            }

            validateStation(f.getDepartureStation());
            validateStation(f.getArrivalStation());

            // Veritabanı NOT NULL alanlarını garantiye al
            if (f.getStatus() == null || f.getStatus().isBlank()) {
                f.setStatus("SCHEDULED");
            }
            if (f.getVersion() == null || f.getVersion() == 0) {
                f.setVersion(1);
            }
            if (f.getIsCurrent() == null) {
                f.setIsCurrent(true);
            }
        }

        List<Flight> savedFlights = flightRepository.saveAll(flights);

        for (Flight f : savedFlights) {
            FlightEvent event = FlightEvent.builder()
                    .eventType("FLIGHT_CREATED")
                    .flightNumber(f.getFlightNumber())
                    .payload(f)
                    .timestamp(LocalDateTime.now())
                    .build();

            kafkaTemplate.send("flight.events", f.getFlightNumber(), event);
            messagingTemplate.convertAndSend("/topic/flights", mapToResponseDto(f));
        }

        return savedFlights;
    }

    @Transactional
    public FlightResponseDto updateFlight(String flightNumber, FlightUpdateRequestDto updateDto) {
        Flight currentFlight = flightRepository.findByFlightNumberAndIsCurrentTrue(flightNumber)
                .orElseThrow(() -> new IllegalArgumentException("Aktif uçuş bulunamadı: " + flightNumber));

        currentFlight.setIsCurrent(false);
        flightRepository.save(currentFlight);

        Flight newVersionFlight = new Flight();
        newVersionFlight.setFlightNumber(currentFlight.getFlightNumber());
        newVersionFlight.setDepartureStation(currentFlight.getDepartureStation());
        newVersionFlight.setArrivalStation(currentFlight.getArrivalStation());

        newVersionFlight.setDepartureTime(
                updateDto.departureTime() != null ? updateDto.departureTime() : currentFlight.getDepartureTime()
        );

        newVersionFlight.setStatus(
                updateDto.status() != null ? updateDto.status() : currentFlight.getStatus()
        );

        newVersionFlight.setVersion(currentFlight.getVersion() + 1);
        newVersionFlight.setIsCurrent(true);

        Flight savedFlight = flightRepository.save(newVersionFlight);

        // FAZ 4: Standart FLIGHT_UPDATED eventi basılıyor
        FlightEvent event = FlightEvent.builder()
                .eventType("FLIGHT_UPDATED")
                .flightNumber(savedFlight.getFlightNumber())
                .payload(savedFlight)
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send("flight.events", savedFlight.getFlightNumber(), event);

        FlightResponseDto responseDto = mapToResponseDto(savedFlight);
        messagingTemplate.convertAndSend("/topic/flights", responseDto);
        return responseDto;
    }

    @Transactional
    public void deleteFlight(Long id) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Silinecek uçuş bulunamadı, ID: " + id));

        flight.setIsCurrent(false);
        flight.setStatus("CANCELLED");
        flightRepository.save(flight);

        // FAZ 4: İptal / Silme durumunda da FLIGHT_UPDATED (CANCELLED) basılıyor
        FlightEvent event = FlightEvent.builder()
                .eventType("FLIGHT_UPDATED")
                .flightNumber(flight.getFlightNumber())
                .payload(flight)
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send("flight.events", flight.getFlightNumber(), event);
        messagingTemplate.convertAndSend("/topic/flights", mapToResponseDto(flight));
    }

    private FlightResponseDto mapToResponseDto(Flight flight) {
        return new FlightResponseDto(
                flight.getId(),
                flight.getFlightNumber(),
                flight.getDepartureStation(),
                flight.getArrivalStation(),
                flight.getDepartureTime(),
                flight.getVersion(),
                flight.getStatus(),
                flight.getIsCurrent()
        );
    }
}