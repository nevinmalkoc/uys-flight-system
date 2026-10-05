package com.tav.uys.flight_service.dto;

import java.io.Serializable;

public class StationDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String iataCode;
    private String city;

    public StationDto() {
    }

    public StationDto(Long id, String name, String iataCode, String city) {
        this.id = id;
        this.name = name;
        this.iataCode = iataCode;
        this.city = city;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIataCode() {
        return iataCode;
    }

    public String getCity() {
        return city;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIataCode(String iataCode) {
        this.iataCode = iataCode;
    }

    public void setCity(String city) {
        this.city = city;
    }
}