package com.matheusosses.api_reservas.reserva;

import com.matheusosses.api_reservas.reserva.dto.NewReservaDto;
import com.matheusosses.api_reservas.reserva.dto.ReservaDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.matheusosses.api_reservas.support.AbstractIntegrationTest;
import com.matheusosses.worker_reservas.WorkerReservasApplication;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ReservaFluxoCompletoIT extends AbstractIntegrationTest {

    private ConfigurableApplicationContext workerContext;

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired
    private ReservaRepository repository;

    @BeforeAll
    void startWorker() {
        var infra = infra();
        workerContext = new SpringApplicationBuilder(WorkerReservasApplication.class)
                .profiles("test")
                .properties(
                        "spring.datasource.url=" + infra.jdbcUrl(),
                        "spring.datasource.username=" + infra.dbUser(),
                        "spring.datasource.password=" + infra.dbPassword(),
                        "spring.rabbitmq.host=" + infra.rabbitHost(),
                        "spring.rabbitmq.port=" + infra.rabbitPort(),
                        "spring.rabbitmq.username=" + infra.rabbitUser(),
                        "spring.rabbitmq.password=" + infra.rabbitPassword())
                .run();
    }

    @AfterAll
    void stopWorker() {
        if (workerContext != null) {
            workerContext.close();
        }
    }

    @Test
    void fluxoCompleto_reservaValida_deveConfirmarNoBanco() throws Exception {
        var dto = new NewReservaDto("Ana", "Catan", LocalDate.now().plusDays(5), 4);

        Long reservaId = postReserva(dto);

        Awaitility.await()
                .atMost(Duration.ofSeconds(15))
                .untilAsserted(() -> {
                    var reserva = repository.findById(reservaId).orElseThrow();
                    assertThat(reserva.getStatus()).isEqualTo(Status.CONFIRMADA);
                });
    }

    @Test
    void fluxoCompleto_reservaInvalida_deveRejeitarNoBanco() throws Exception {
        var dto = new NewReservaDto("Bob", "Azul", LocalDate.now().minusDays(1), 4);

        Long reservaId = postReserva(dto);

        Awaitility.await()
                .atMost(Duration.ofSeconds(15))
                .untilAsserted(() -> {
                    var reserva = repository.findById(reservaId).orElseThrow();
                    assertThat(reserva.getStatus()).isEqualTo(Status.REJEITADA);
                });
    }

    private Long postReserva(NewReservaDto dto) throws Exception {
        var responseBody = mockMvc.perform(post("/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readValue(responseBody, ReservaDto.class).id();
    }
}
