package com.tav.uys.referencemanager.service;

import com.tav.uys.referencemanager.entity.Route;
import com.tav.uys.referencemanager.event.ReferenceEvent;
import com.tav.uys.referencemanager.repository.RouteRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class RouteService {

    private final RouteRepository routeRepository;
    private final ReferenceEventProducer eventProducer;

    public RouteService(RouteRepository routeRepository, ReferenceEventProducer eventProducer) {
        this.routeRepository = routeRepository;
        this.eventProducer = eventProducer;
    }

    @CacheEvict(value = "routes", allEntries = true)
    public Route createRoute(Route route) {
        Route saved = routeRepository.save(route);
        eventProducer.publishEvent(ReferenceEvent.builder()
                .eventType("CREATED")
                .entityType("ROUTE")
                .entityId(saved.getId())
                .payload(saved)
                .timestamp(LocalDateTime.now())
                .build());
        return saved;
    }

    @Cacheable(value = "routes")
    public List<Route> getAllRoutes() {
        return routeRepository.findAll();
    }

    @Cacheable(value = "routes", key = "#id")
    public Optional<Route> getRouteById(Long id) {
        return routeRepository.findById(id);
    }

    @CacheEvict(value = "routes", allEntries = true)
    public void deleteRoute(Long id) {
        routeRepository.deleteById(id);
        eventProducer.publishEvent(ReferenceEvent.builder()
                .eventType("DELETED")
                .entityType("ROUTE")
                .entityId(id)
                .timestamp(LocalDateTime.now())
                .build());
    }
}
