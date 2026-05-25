package com.matheusosses.api_reservas.reserva.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReservaDto(
    Long id,
    String nomeJogador,
    String nomeJogo,
    LocalDate dataPartida,
    Integer quantidadeJogadores,
    String status,
    LocalDateTime createdAt
) {
}
