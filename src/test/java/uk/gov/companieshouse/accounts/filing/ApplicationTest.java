package uk.gov.companieshouse.accounts.filing;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.ApplicationContext;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApplicationTest {

    @Container
    @ServiceConnection
    static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:8.2.5-noble");

    @Test
    void shouldLoadContext(@Autowired ApplicationContext applicationContext) {
        assertThat(applicationContext).isNotNull();
    }
}
