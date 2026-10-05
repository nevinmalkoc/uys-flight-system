package com.tav.uys.flight_service.client;

import com.tav.uys.flight_service.dto.StationDto;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class ReferenceClient {

    private final RestClient restClient;

    public ReferenceClient() {
        String baseUrl = System.getenv().getOrDefault("REFERENCE_SERVICE_URL", "http://localhost:8083/api/v1");
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Cacheable(value = "flight_service_stations")
    public List<StationDto> getAllStations() {
        System.out.println(">>> FLIGHT-SERVICE REDIS MISS: Istasyonlar Reference-Manager'dan cekilip Flight-Service Redis onbellegine yaziliyor!");
        return restClient.get()
                .uri("/stations")
                .retrieve()
                .body(new ParameterizedTypeReference<List<StationDto>>() {});
    }
}