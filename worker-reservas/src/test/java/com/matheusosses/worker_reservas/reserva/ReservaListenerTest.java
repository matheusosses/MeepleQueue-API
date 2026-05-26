package com.matheusosses.worker_reservas.reserva;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservaListenerTest {

    @Mock
    private ReservaRepository repository;

    @Mock
    private ReservaValidator validator;

    @InjectMocks
    private ReservaListener listener;

    @Test
    void consumir_idInexistente_naoDeveSalvar() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        listener.consumir(99L);

        verify(repository, never()).save(any());
        verify(validator, never()).validar(any());
    }

    @Test
    void consumir_reservaExistente_deveAtualizarStatusESalvar() {
        var reserva = new Reserva();
        reserva.setId(1L);
        reserva.setDataPartida(LocalDate.now().plusDays(1));
        reserva.setQuantidadeJogadores(4);
        reserva.setStatus(Status.PENDENTE);

        when(repository.findById(1L)).thenReturn(Optional.of(reserva));
        when(validator.validar(reserva)).thenReturn(Status.CONFIRMADA);

        listener.consumir(1L);

        assertThat(reserva.getStatus()).isEqualTo(Status.CONFIRMADA);
        verify(repository).save(reserva);
    }

    @Test
    void consumir_reservaInvalida_deveRejeitar() {
        var reserva = new Reserva();
        reserva.setId(2L);
        reserva.setStatus(Status.PENDENTE);

        when(repository.findById(2L)).thenReturn(Optional.of(reserva));
        when(validator.validar(reserva)).thenReturn(Status.REJEITADA);

        listener.consumir(2L);

        assertThat(reserva.getStatus()).isEqualTo(Status.REJEITADA);
        verify(repository).save(reserva);
    }
}
