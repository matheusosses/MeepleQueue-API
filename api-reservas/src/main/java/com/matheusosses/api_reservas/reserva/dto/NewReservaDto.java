package com.matheusosses.api_reservas.reserva.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record NewReservaDto(
    @Schema(example = "Jogador") String nomeJogador,
    @Schema(example = "Root") String nomeJogo,
    @Schema(example = "2026-06-15") LocalDate dataPartida,
    @Schema(example = "4") Integer quantidadeJogadores
) {
}
