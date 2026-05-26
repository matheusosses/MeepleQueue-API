package com.matheusosses.worker_reservas.reserva;

import com.matheusosses.worker_reservas.support.AbstractIntegrationTest;
import com.matheusosses.worker_reservas.support.RabbitTestConfig;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(RabbitTestConfig.class)
class ReservaListenerIntegrationIT extends AbstractIntegrationTest {

    @Autowired
    private ReservaRepository repository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    void consumir_reservaValida_deveConfirmar() {
        var reserva = salvarPendente(LocalDate.now().plusDays(2), 4);

        publicarMensagem(reserva.getId());

        awaitStatus(reserva.getId(), Status.CONFIRMADA);
    }

    @Test
    void consumir_dataPassada_deveRejeitar() {
        var reserva = salvarPendente(LocalDate.now().minusDays(1), 4);

        publicarMensagem(reserva.getId());

        awaitStatus(reserva.getId(), Status.REJEITADA);
    }

    @Test
    void consumir_quantidadeZero_deveRejeitar() {
        var reserva = salvarPendente(LocalDate.now().plusDays(2), 0);

        publicarMensagem(reserva.getId());

        awaitStatus(reserva.getId(), Status.REJEITADA);
    }

    @Test
    void consumir_idInexistente_naoDeveAlterarBanco() {
        long totalAntes = repository.count();

        publicarMensagem(99999L);

        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .pollDelay(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> assertThat(repository.count()).isEqualTo(totalAntes));
    }

    private Reserva salvarPendente(LocalDate dataPartida, int quantidadeJogadores) {
        var reserva = new Reserva();
        reserva.setNomeJogador("Teste");
        reserva.setNomeJogo("Catan");
        reserva.setDataPartida(dataPartida);
        reserva.setQuantidadeJogadores(quantidadeJogadores);
        reserva.setStatus(Status.PENDENTE);
        return repository.save(reserva);
    }

    private void publicarMensagem(Long reservaId) {
        rabbitTemplate.convertAndSend(
                RabbitTestConfig.EXCHANGE_NAME,
                RabbitTestConfig.ROUTING_KEY,
                reservaId);
    }

    private void awaitStatus(Long id, Status statusEsperado) {
        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    var reserva = repository.findById(id).orElseThrow();
                    assertThat(reserva.getStatus()).isEqualTo(statusEsperado);
                });
    }
}
