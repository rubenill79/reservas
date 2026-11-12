package com.rubenlazaro.reservas;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Starts a throwaway PostgreSQL for the tests. {@code @ServiceConnection} points the
 * application's datasource at it, so the tests never touch the development database.
 * Flyway runs the real migrations against it, and {@code ddl-auto: validate} checks the
 * JPA entities against the resulting schema.
 */
@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        // fsync=off is the Testcontainers default for speed (setting a command replaces it).
        // deadlock_timeout is how long PostgreSQL waits before checking for a deadlock; the 1 s
        // default would make every concurrent-insert test iteration take over a second.
        return new PostgreSQLContainer(DockerImageName.parse("postgres:16"))
                .withCommand("postgres", "-c", "fsync=off", "-c", "deadlock_timeout=100ms");
    }
}
