package com.tav.uys.flight_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "flights")
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String flightNumber;
    private String departureStation;
    private String arrivalStation;
    private LocalDateTime departureTime;

    private Integer version;
    private String status;
    private Boolean isCurrent;

    @PrePersist
    public void prePersist() {
        if (this.version == null) {
            this.version = 1;
        }
        if (this.status == null) {
            this.status = "SCHEDULED";
        }
        if (this.isCurrent == null) {
            this.isCurrent = true;
        }
    }

    public Flight() {
    }

    public Flight(Long id, String flightNumber, String departureStation, String arrivalStation,
                  LocalDateTime departureTime, Integer version, String status, Boolean isCurrent) {
        this.id = id;
        this.flightNumber = flightNumber;
        this.departureStation = departureStation;
        this.arrivalStation = arrivalStation;
        this.departureTime = departureTime;
        this.version = version;
        this.status = status;
        this.isCurrent = isCurrent;
    }

    public Long getId() {
        return id;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public String getDepartureStation() {
        return departureStation;
    }

    public String getArrivalStation() {
        return arrivalStation;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public Integer getVersion() {
        return version;
    }

    public String getStatus() {
        return status;
    }

    public Boolean getIsCurrent() {
        return isCurrent;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public void setDepartureStation(String departureStation) {
        this.departureStation = departureStation;
    }

    public void setArrivalStation(String arrivalStation) {
        this.arrivalStation = arrivalStation;
    }

    public void setDepartureTime(LocalDateTime departureTime) {
        this.departureTime = departureTime;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setIsCurrent(Boolean isCurrent) {
        this.isCurrent = isCurrent;
    }
}