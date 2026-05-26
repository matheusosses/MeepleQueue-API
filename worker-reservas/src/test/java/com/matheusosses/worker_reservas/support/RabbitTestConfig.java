package com.matheusosses.worker_reservas.support;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitTestConfig {

    public static final String EXCHANGE_NAME = "reservas.ex";
    public static final String QUEUE_NAME = "processar-reserva.queue";
    public static final String ROUTING_KEY = "reserva.solicitada";

    @Bean
    public DirectExchange reservasExchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue processarReservaQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public Binding binding(Queue processarReservaQueue, DirectExchange reservasExchange) {
        return BindingBuilder
                .bind(processarReservaQueue)
                .to(reservasExchange)
                .with(ROUTING_KEY);
    }
}
