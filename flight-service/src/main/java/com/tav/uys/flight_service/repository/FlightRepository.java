package com.tav.uys.flight_service.repository;

import com.tav.uys.flight_service.entity.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {

    // Uçuş numarasına göre aktif (güncel) olan kaydı getirir
    Optional<Flight> findByFlightNumberAndIsCurrentTrue(String flightNumber);

    // Sadece güncel uçuşları listelemek istersek
    List<Flight> findByIsCurrentTrue();

    // Bir uçuşun tüm geçmiş versiyonlarını görmek istersek
    List<Flight> findByFlightNumberOrderByVersionDesc(String flightNumber);
}