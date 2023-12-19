package uk.gov.companieshouse.accounts.filing.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HealthCheckControllerTest {

    HealthCheckController controller;

    @BeforeEach
    void setup() {
        controller = new HealthCheckController();
    }

    @Test
    void testHealthCheck() {
        ResponseEntity<String> response = controller.checking();

        assertEquals(200, response.getStatusCode().value());
        assertEquals("OK", response.getBody());
    }
}
