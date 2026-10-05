package com.tav.uys.referencemanager.repository;

import com.tav.uys.referencemanager.entity.FlightType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FlightTypeRepository extends JpaRepository<FlightType, Long> {
}
