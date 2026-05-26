package com.matheusosses.worker_reservas.reserva;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class ReservaListener {

    private final ReservaRepository repository;

    @RabbitListener(queues = "processar-reserva.queue")
    public void consumir(Long reservaId) {
        System.out.println("ℹ️ Mensagem recebida! Analisando viabilidade da reserva ID: " + reservaId);
        repository.findById(reservaId).ifPresent((reserva -> {
            boolean isDataValid = !reserva.getDataPartida().isBefore(LocalDate.now());
            boolean isQuantidadeValid = reserva.getQuantidadeJogadores() > 0
                && reserva.getQuantidadeJogadores() <= 8;

            if(isDataValid && isQuantidadeValid) {
                System.out.println("Reserva confirmada!!");
                reserva.setStatus(Status.CONFIRMADA);
            } else {
                System.out.println("Reserva rejeitada!!");
                reserva.setStatus(Status.REJEITADA);
            }

            repository.save(reserva);
        }));
    }
}
