package com.tav.uys.referencemanager.service;

import com.tav.uys.referencemanager.entity.Station;
import com.tav.uys.referencemanager.event.ReferenceEvent;
import com.tav.uys.referencemanager.repository.StationRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StationService {

    private final StationRepository stationRepository;
    private final ReferenceEventProducer eventProducer;

    public StationService(StationRepository stationRepository, ReferenceEventProducer eventProducer) {
        this.stationRepository = stationRepository;
        this.eventProducer = eventProducer;
    }

    @CacheEvict(value = "stations", allEntries = true)
    public Station createStation(Station station) {
        Station savedStation = stationRepository.save(station);

        eventProducer.publishEvent(ReferenceEvent.builder()
                .eventType("CREATED")
                .entityType("STATION")
                .entityId(savedStation.getId())
                .payload(savedStation)
                .timestamp(LocalDateTime.now())
                .build());

        return savedStation;
    }

    @CacheEvict(value = "stations", allEntries = true)
    public void deleteStation(Long id) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("İstasyon bulunamadı: " + id));

        stationRepository.delete(station);

        // Kafka'ya DELETED eventi fırlatıyoruz -> Flight Service Redis'ten bu anahtarı silecek (Evict)
        eventProducer.publishEvent(ReferenceEvent.builder()
                .eventType("DELETED")
                .entityType("STATION")
                .entityId(station.getId())
                .payload(station)
                .timestamp(LocalDateTime.now())
                .build());
    }

    @Cacheable(value = "stations")
    public List<Station> getAllStations() {
        return stationRepository.findAll();
    }
}
