package com.matheusosses.api_reservas.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

public abstract class AbstractIntegrationTest {

    private static final String MYSQL_IMAGE = "mysql:8.0";
    private static final String RABBITMQ_IMAGE = "rabbitmq:3-management";

    private static final boolean USE_TESTCONTAINERS = isDockerAvailable();

    private static final MySQLContainer<?> MYSQL_CONTAINER;
    private static final RabbitMQContainer RABBIT_CONTAINER;

    static {
        if (USE_TESTCONTAINERS) {
            MYSQL_CONTAINER = new MySQLContainer<>(DockerImageName.parse(MYSQL_IMAGE))
                    .withDatabaseName("boardgames_db")
                    .withUsername("admin")
                    .withPassword("admin");
            RABBIT_CONTAINER = new RabbitMQContainer(DockerImageName.parse(RABBITMQ_IMAGE))
                    .withUser("admin", "admin");
            MYSQL_CONTAINER.start();
            RABBIT_CONTAINER.start();
        } else {
            MYSQL_CONTAINER = null;
            RABBIT_CONTAINER = null;
        }
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        if (USE_TESTCONTAINERS) {
            registry.add("spring.datasource.url", MYSQL_CONTAINER::getJdbcUrl);
            registry.add("spring.datasource.username", MYSQL_CONTAINER::getUsername);
            registry.add("spring.datasource.password", MYSQL_CONTAINER::getPassword);

            registry.add("spring.rabbitmq.host", RABBIT_CONTAINER::getHost);
            registry.add("spring.rabbitmq.port", () -> String.valueOf(RABBIT_CONTAINER.getAmqpPort()));
            registry.add("spring.rabbitmq.username", RABBIT_CONTAINER::getAdminUsername);
            registry.add("spring.rabbitmq.password", RABBIT_CONTAINER::getAdminPassword);
        } else {
            registry.add("spring.datasource.url", () -> "jdbc:mysql://localhost:3307/boardgames_db");
            registry.add("spring.datasource.username", () -> "admin");
            registry.add("spring.datasource.password", () -> "admin");

            registry.add("spring.rabbitmq.host", () -> "localhost");
            registry.add("spring.rabbitmq.port", () -> "5672");
            registry.add("spring.rabbitmq.username", () -> "admin");
            registry.add("spring.rabbitmq.password", () -> "admin");
        }
    }

    protected static IntegrationInfra infra() {
        if (USE_TESTCONTAINERS) {
            return new IntegrationInfra(
                    MYSQL_CONTAINER.getJdbcUrl(),
                    MYSQL_CONTAINER.getUsername(),
                    MYSQL_CONTAINER.getPassword(),
                    RABBIT_CONTAINER.getHost(),
                    String.valueOf(RABBIT_CONTAINER.getAmqpPort()),
                    RABBIT_CONTAINER.getAdminUsername(),
                    RABBIT_CONTAINER.getAdminPassword());
        }
        return new IntegrationInfra(
                "jdbc:mysql://localhost:3307/boardgames_db",
                "admin",
                "admin",
                "localhost",
                "5672",
                "admin",
                "admin");
    }

    private static boolean isDockerAvailable() {
        try {
            org.testcontainers.DockerClientFactory.instance().client();
            return true;
        } catch (Throwable ex) {
            return false;
        }
    }

    public record IntegrationInfra(
            String jdbcUrl,
            String dbUser,
            String dbPassword,
            String rabbitHost,
            String rabbitPort,
            String rabbitUser,
            String rabbitPassword) {
    }
}
