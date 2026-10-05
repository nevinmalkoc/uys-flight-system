package com.tav.uys.flight_service.mapper;

import com.tav.uys.flight_service.dto.FlightCreateRequestDto;
import com.tav.uys.flight_service.dto.FlightResponseDto;
import com.tav.uys.flight_service.entity.Flight;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FlightMapper {

    Flight toEntity(FlightCreateRequestDto dto);

    FlightResponseDto toDto(Flight entity);

    List<FlightResponseDto> toDtoList(List<Flight> entities);
}