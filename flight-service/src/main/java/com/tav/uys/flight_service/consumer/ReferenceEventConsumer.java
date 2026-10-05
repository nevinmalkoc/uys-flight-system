package com.tav.uys.flight_service.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tav.uys.flight_service.service.ReferenceCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ReferenceEventConsumer {

    private final ReferenceCacheService cacheService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ReferenceEventConsumer(ReferenceCacheService cacheService) {
        this.cacheService = cacheService;
    }

    @KafkaListener(topics = "reference.events", groupId = "flight-service-cache-group")
    public void handleReferenceEvent(String message) {
        log.info(">>> KAFKA GELEN MESAJ: {}", message);
        try {
            JsonNode root = objectMapper.readTree(message);
            String eventType = root.path("eventType").asText("CREATED");
            String entityType = root.path("entityType").asText("");
            JsonNode payload = root.path("payload");

            String code = extractCode(entityType, payload);

            if (code != null && !code.isBlank()) {
                if ("DELETED".equalsIgnoreCase(eventType)) {
                    cacheService.evict(entityType, code);
                } else {
                    cacheService.put(entityType, code, "VALID");
                }
            }
        } catch (Exception e) {
            log.error("Kafka mesajı işlenirken hata oluştu: {}", e.getMessage());
        }
    }

    private String extractCode(String entityType, JsonNode payload) {
        if (payload == null || payload.isNull()) return null;

        return switch (entityType.toUpperCase()) {
            case "STATION" -> payload.has("iataCode") ? payload.get("iataCode").asText() : payload.path("iata_code").asText(null);
            case "AIRLINE" -> payload.has("code") ? payload.get("code").asText() : payload.path("iataCode").asText(null);
            case "AIRCRAFT" -> payload.has("registration") ? payload.get("registration").asText() : payload.path("type").asText(null);
            case "ROUTE" -> {
                String origin = payload.path("origin").asText(null);
                String dest = payload.path("destination").asText(null);
                yield (origin != null && dest != null) ? origin + "-" + dest : null;
            }
            default -> payload.has("code") ? payload.get("code").asText() : null;
        };
    }
}
