package com.tav.uys.flight_archive_service.repository;

import com.tav.uys.flight_archive_service.entity.FlightArchive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface FlightArchiveRepository extends JpaRepository<FlightArchive, Long> {

    Page<FlightArchive> findByFlightNumber(String flightNumber, Pageable pageable);

    List<FlightArchive> findByFlightNumberOrderByVersionAsc(String flightNumber);

    // Gecikmeye uğramış uçuşları listeler
    List<FlightArchive> findByStatus(String status);

    // Raporlama / KPI: Statülere göre toplam uçuş olay dağılımı
    @Query("SELECT f.status as status, COUNT(f) as count FROM FlightArchive f GROUP BY f.status")
    List<Map<String, Object>> countFlightsByStatus();
}