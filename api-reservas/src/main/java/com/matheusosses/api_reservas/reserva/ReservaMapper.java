package com.matheusosses.api_reservas.reserva;

import com.matheusosses.api_reservas.reserva.dto.NewReservaDto;
import com.matheusosses.api_reservas.reserva.dto.ReservaDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReservaMapper {

    Reserva toEntity(NewReservaDto dto);

    ReservaDto toDto(Reserva reserva);
}
