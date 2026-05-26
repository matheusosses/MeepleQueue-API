package com.matheusosses.api_reservas.reserva;

import com.matheusosses.api_reservas.reserva.dto.NewReservaDto;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ReservaMapperTest {

    private final ReservaMapper mapper = new ReservaMapperImpl();

    @Test
    void toEntity_deveMapearCamposDoDto() {
        var dto = new NewReservaDto("Ana", "Catan", LocalDate.of(2026, 6, 1), 4);

        var entity = mapper.toEntity(dto);

        assertThat(entity.getNomeJogador()).isEqualTo("Ana");
        assertThat(entity.getNomeJogo()).isEqualTo("Catan");
        assertThat(entity.getDataPartida()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(entity.getQuantidadeJogadores()).isEqualTo(4);
    }

    @Test
    void toDto_deveMapearCamposDaEntidade() {
        var entity = new Reserva();
        entity.setId(1L);
        entity.setNomeJogador("Ana");
        entity.setNomeJogo("Catan");
        entity.setDataPartida(LocalDate.of(2026, 6, 1));
        entity.setQuantidadeJogadores(4);
        entity.setStatus(Status.PENDENTE);
        entity.setCreatedAt(LocalDateTime.of(2026, 5, 26, 10, 0));

        var dto = mapper.toDto(entity);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.nomeJogador()).isEqualTo("Ana");
        assertThat(dto.status()).isEqualTo("PENDENTE");
    }
}
