package com.matheusosses.api_reservas.reserva;

import com.matheusosses.api_reservas.reserva.dto.NewReservaDto;
import com.matheusosses.api_reservas.reserva.dto.ReservaDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservaController.class)
class ReservaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservaService service;

    @Test
    void solicitarReserva_deveRetornar201ComCorpo() throws Exception {
        var dto = new NewReservaDto("Ana", "Catan", LocalDate.of(2026, 6, 1), 4);
        var response = new ReservaDto(1L, "Ana", "Catan", dto.dataPartida(), 4, "PENDENTE", LocalDateTime.now());

        when(service.solicitarReserva(any(NewReservaDto.class))).thenReturn(response);

        mockMvc.perform(post("/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nomeJogador": "Ana",
                                  "nomeJogo": "Catan",
                                  "dataPartida": "2026-06-01",
                                  "quantidadeJogadores": 4
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nomeJogador").value("Ana"))
                .andExpect(jsonPath("$.status").value("PENDENTE"));
    }
}
