package com.matheusosses.api_reservas.reserva.dto;

import java.time.LocalDate;

public record NewReservaDto(
    String nomeJogador,
    String nomeJogo,
    LocalDate dataPartida,
    Integer quantidadeJogadores
) {
}
