package com.tav.uys.flight_archive_service.listener;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tav.uys.flight_archive_service.entity.FlightArchive;
import com.tav.uys.flight_archive_service.repository.FlightArchiveRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class FlightEventListener {

    private final FlightArchiveRepository archiveRepository;
    private final ObjectMapper objectMapper;

    public FlightEventListener(FlightArchiveRepository archiveRepository) {
        this.archiveRepository = archiveRepository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @KafkaListener(topics = "flight.events", groupId = "flight-archive-group")
    public void handleFlightEvent(String message) {
        try {
            JsonNode rootNode = objectMapper.readTree(message);
            JsonNode payloadNode = rootNode.has("payload") ? rootNode.get("payload") : rootNode;

            FlightArchive flight = objectMapper.treeToValue(payloadNode, FlightArchive.class);
            flight.setId(null);
            flight.setArchivedAt(LocalDateTime.now());

            FlightArchive saved = archiveRepository.save(flight);
            System.out.println(">>> [FAS] Arşive Kaydedildi -> ID: " + saved.getId() 
                    + " | Uçuş: " + saved.getFlightNumber() 
                    + " | Versiyon: " + saved.getVersion() 
                    + " | Durum: " + saved.getStatus());
        } catch (Exception e) {
            System.err.println(">>> [FAS] JSON Parse Hatası: " + e.getMessage());
        }
    }
}
