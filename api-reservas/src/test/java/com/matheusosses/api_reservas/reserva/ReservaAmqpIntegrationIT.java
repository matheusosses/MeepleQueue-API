package com.matheusosses.api_reservas.reserva;

import com.matheusosses.api_reservas.config.RabbitMQConfig;
import com.matheusosses.api_reservas.reserva.dto.NewReservaDto;
import com.matheusosses.api_reservas.reserva.dto.ReservaDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.matheusosses.api_reservas.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReservaAmqpIntegrationIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired
    private ReservaRepository repository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    void solicitarReserva_devePersistirComoPendenteEPublicarIdNaFila() throws Exception {
        var dto = new NewReservaDto("Ana", "Catan", LocalDate.now().plusDays(3), 4);

        var responseBody = mockMvc.perform(post("/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var reservaDto = objectMapper.readValue(responseBody, ReservaDto.class);
        Long reservaId = reservaDto.id();
        var reserva = repository.findById(reservaId).orElseThrow();
        assertThat(reserva.getStatus()).isEqualTo(Status.PENDENTE);

        Object mensagem = rabbitTemplate.receiveAndConvert(RabbitMQConfig.QUEUE_NAME, 5000);
        assertThat(mensagem).isEqualTo(reservaId);
    }
}
