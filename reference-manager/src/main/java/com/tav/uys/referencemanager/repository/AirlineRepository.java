package com.tav.uys.referencemanager.repository;

import com.tav.uys.referencemanager.entity.Airline;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AirlineRepository extends JpaRepository<Airline, Long> {
}