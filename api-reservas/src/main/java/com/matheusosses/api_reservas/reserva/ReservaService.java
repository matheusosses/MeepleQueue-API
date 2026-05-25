package com.matheusosses.api_reservas.reserva;

import com.matheusosses.api_reservas.reserva.dto.NewReservaDto;
import com.matheusosses.api_reservas.reserva.dto.ReservaDto;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservaService {

    private final ReservaRepository repository;
    private final RabbitTemplate rabbitTemplate;
    private final ReservaMapper mapper;

    @Transactional
    public ReservaDto solicitarReserva(NewReservaDto dto) {
        Reserva reserva = mapper.toEntity(dto);
        reserva.setStatus(Status.PENDENTE);

        Reserva reservaSalva = repository.save(reserva);

        rabbitTemplate.convertAndSend("reservas.ex", "reserva.solicitada", reservaSalva.getId());
        return mapper.toDto(reservaSalva);
    }
}
