package com.matheusosses.api_reservas.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI meepleQueueOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("MeepleQueue API")
                .description("API para solicitação de reservas de mesas em tavernas de Board Games. " +
                    "As solicitações são processadas de forma assíncrona via RabbitMQ.")
                .version("v1.0")
                .contact(new Contact()
                    .name("Matheus Osses")
                    .email("maths0ss3s@gmail.com")));
    }
}
