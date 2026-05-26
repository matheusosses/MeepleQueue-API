package com.matheusosses.worker_reservas.reserva;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservaListener {

    private final ReservaRepository repository;
    private final ReservaValidator validator;

    @RabbitListener(queues = "processar-reserva.queue")
    public void consumir(Long reservaId) {
        System.out.println("ℹ️ Mensagem recebida! Analisando viabilidade da reserva ID: " + reservaId);
        repository.findById(reservaId).ifPresent(reserva -> {
            Status status = validator.validar(reserva);
            if (status == Status.CONFIRMADA) {
                System.out.println("Reserva confirmada!!");
            } else {
                System.out.println("Reserva rejeitada!!");
            }
            reserva.setStatus(status);
            repository.save(reserva);
        });
    }
}
