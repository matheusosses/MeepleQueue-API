package com.matheusosses.worker_reservas.reserva;

import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ReservaValidator {

    public Status validar(Reserva reserva) {
        boolean dataOk = !reserva.getDataPartida().isBefore(LocalDate.now());
        boolean qtdOk = reserva.getQuantidadeJogadores() > 0
                && reserva.getQuantidadeJogadores() <= 8;
        return dataOk && qtdOk ? Status.CONFIRMADA : Status.REJEITADA;
    }
}
