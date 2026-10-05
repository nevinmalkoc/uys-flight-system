package com.tav.uys.flight_service;

import com.tav.uys.flight_service.dto.FlightCreateRequestDto;
import com.tav.uys.flight_service.dto.FlightResponseDto;
import com.tav.uys.flight_service.dto.FlightUpdateRequestDto;
import com.tav.uys.flight_service.entity.Flight;
import com.tav.uys.flight_service.repository.FlightRepository;
import com.tav.uys.flight_service.service.FlightService;
import com.tav.uys.flight_service.service.ReferenceCacheService;
import com.tav.uys.flight_service.client.ReferenceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlightServiceTest {

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private ReferenceClient referenceClient;

    @Mock
    private ReferenceCacheService cacheService;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private FlightService flightService;

    private FlightCreateRequestDto validCreateRequest;

    @BeforeEach
    void setUp() {
        validCreateRequest = new FlightCreateRequestDto(
                "TK1923",
                "IST",
                "ESB",
                LocalDateTime.now().plusHours(2)
        );
    }

    @Test
    @DisplayName("UC-01 & UC-03: İstasyonlar Redis'te varsa uçuş başarıyla oluşturulmalı")
    void shouldCreateFlightSuccessfullyWhenStationsExistInCache() {
        when(cacheService.exists(eq("STATION"), anyString())).thenReturn(true);

        Flight mockSavedFlight = new Flight();
        mockSavedFlight.setId(1L);
        mockSavedFlight.setFlightNumber("TK1923");
        mockSavedFlight.setDepartureStation("IST");
        mockSavedFlight.setArrivalStation("ESB");
        mockSavedFlight.setVersion(1);

        when(flightRepository.save(any(Flight.class))).thenReturn(mockSavedFlight);

        FlightResponseDto response = flightService.createFlight(validCreateRequest);

        assertNotNull(response);
        assertEquals("TK1923", response.flightNumber());
        verify(flightRepository, times(1)).save(any(Flight.class));
    }

    @Test
    @DisplayName("UC-04: İstasyon sistemde kayıtlı değilse hata fırlatılmalı")
    void shouldThrowExceptionWhenStationNotFoundAnywhere() {
        when(cacheService.exists(eq("STATION"), anyString())).thenReturn(false);
        when(referenceClient.getAllStations()).thenReturn(Collections.emptyList());

        assertThrows(IllegalArgumentException.class, () -> {
            flightService.createFlight(validCreateRequest);
        });

        verify(flightRepository, never()).save(any(Flight.class));
    }

    @Test
    @DisplayName("UC-05 & UC-06: Uçuş durumu güncellendiğinde yeni versiyon kaydedilmeli ve Kafka eventi basılmalı")
    void shouldUpdateFlightAndPublishKafkaEvent() {
        Flight existingFlight = new Flight();
        existingFlight.setId(10L);
        existingFlight.setFlightNumber("TK1923");
        existingFlight.setDepartureStation("IST");
        existingFlight.setArrivalStation("ESB");
        existingFlight.setDepartureTime(LocalDateTime.now().plusHours(1));
        existingFlight.setStatus("SCHEDULED");
        existingFlight.setVersion(1);
        existingFlight.setIsCurrent(true);

        when(flightRepository.findByFlightNumberAndIsCurrentTrue("TK1923"))
                .thenReturn(Optional.of(existingFlight));

        when(flightRepository.save(any(Flight.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Parametre sırası: (departureTime, status) -> (null, "DELAYED")
        FlightUpdateRequestDto updateDto = new FlightUpdateRequestDto(null, "DELAYED");

        FlightResponseDto response = flightService.updateFlight("TK1923", updateDto);

        assertNotNull(response);
        assertEquals("DELAYED", response.status());
        assertEquals(2, response.version());
        verify(kafkaTemplate, times(1)).send(eq("flight.events"), eq("TK1923"), any());
    }
}
