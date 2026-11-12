package com.rubenlazaro.reservas;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Base class for tests that need the full application, a real PostgreSQL and MockMvc. All
 * subclasses share the same configuration, so Spring caches one context and one container
 * for the whole run.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public abstract class IntegrationTest {

    @Autowired
    protected JdbcTemplate jdbc;

    @BeforeEach
    protected void cleanDatabase() {
        jdbc.execute("TRUNCATE TABLE booking, resource RESTART IDENTITY CASCADE");
    }
}
