package com.matheusosses.worker_reservas.reserva;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ReservaValidatorTest {

    private final ReservaValidator validator = new ReservaValidator();

    @Test
    void validar_reservaValida_deveConfirmar() {
        var reserva = reserva(LocalDate.now().plusDays(1), 4);

        assertThat(validator.validar(reserva)).isEqualTo(Status.CONFIRMADA);
    }

    @Test
    void validar_dataHoje_deveConfirmar() {
        var reserva = reserva(LocalDate.now(), 2);

        assertThat(validator.validar(reserva)).isEqualTo(Status.CONFIRMADA);
    }

    @Test
    void validar_dataPassada_deveRejeitar() {
        var reserva = reserva(LocalDate.now().minusDays(1), 4);

        assertThat(validator.validar(reserva)).isEqualTo(Status.REJEITADA);
    }

    @Test
    void validar_quantidadeZero_deveRejeitar() {
        var reserva = reserva(LocalDate.now().plusDays(1), 0);

        assertThat(validator.validar(reserva)).isEqualTo(Status.REJEITADA);
    }

    @Test
    void validar_quantidadeMaiorQueOito_deveRejeitar() {
        var reserva = reserva(LocalDate.now().plusDays(1), 9);

        assertThat(validator.validar(reserva)).isEqualTo(Status.REJEITADA);
    }

    @Test
    void validar_dataPassadaEQuantidadeInvalida_deveRejeitar() {
        var reserva = reserva(LocalDate.now().minusDays(1), 0);

        assertThat(validator.validar(reserva)).isEqualTo(Status.REJEITADA);
    }

    private Reserva reserva(LocalDate dataPartida, int quantidadeJogadores) {
        var reserva = new Reserva();
        reserva.setDataPartida(dataPartida);
        reserva.setQuantidadeJogadores(quantidadeJogadores);
        return reserva;
    }
}
