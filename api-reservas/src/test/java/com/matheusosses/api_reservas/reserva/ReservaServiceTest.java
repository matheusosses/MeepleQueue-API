package com.matheusosses.api_reservas.reserva;

import com.matheusosses.api_reservas.config.RabbitMQConfig;
import com.matheusosses.api_reservas.reserva.dto.NewReservaDto;
import com.matheusosses.api_reservas.reserva.dto.ReservaDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock
    private ReservaRepository repository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private ReservaMapper mapper;

    @InjectMocks
    private ReservaService service;

    @Test
    void solicitarReserva_deveSalvarComoPendenteEPublicarMensagem() {
        var dto = new NewReservaDto("Ana", "Catan", LocalDate.now().plusDays(1), 4);
        var entity = new Reserva();
        entity.setNomeJogador("Ana");
        entity.setNomeJogo("Catan");
        entity.setDataPartida(dto.dataPartida());
        entity.setQuantidadeJogadores(4);

        var saved = new Reserva();
        saved.setId(1L);
        saved.setNomeJogador("Ana");
        saved.setNomeJogo("Catan");
        saved.setDataPartida(dto.dataPartida());
        saved.setQuantidadeJogadores(4);
        saved.setStatus(Status.PENDENTE);
        saved.setCreatedAt(LocalDateTime.now());

        var responseDto = new ReservaDto(1L, "Ana", "Catan", dto.dataPartida(), 4, "PENDENTE", saved.getCreatedAt());

        when(mapper.toEntity(dto)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(saved);
        when(mapper.toDto(saved)).thenReturn(responseDto);

        var result = service.solicitarReserva(dto);

        assertThat(entity.getStatus()).isEqualTo(Status.PENDENTE);
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq(RabbitMQConfig.ROUTING_KEY),
                eq(1L));
        assertThat(result).isEqualTo(responseDto);
    }
}
