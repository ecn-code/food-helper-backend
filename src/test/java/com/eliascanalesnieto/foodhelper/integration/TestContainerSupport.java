package com.eliascanalesnieto.foodhelper.integration;

import java.util.List;
import org.testcontainers.containers.PostgreSQLContainer;

final class TestContainerSupport {
    private static final String POSTGRES_PORT_ENV = "TESTCONTAINERS_POSTGRES_HOST_PORT";

    private TestContainerSupport() {
    }

    static PostgreSQLContainer<?> postgres(String imageName) {
        PostgreSQLContainer<?> container = new PostgreSQLContainer<>(imageName)
                .withDatabaseName("foodhelper")
                .withUsername("foodhelper")
                .withPassword("foodhelper")
                .withInitScript("db/test-init.sql");

        String fixedPort = System.getenv(POSTGRES_PORT_ENV);
        if (fixedPort != null && !fixedPort.isBlank()) {
            container.setPortBindings(List.of(fixedPort + ":5432"));
        }

        return container;
    }
}
