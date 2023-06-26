package uk.gov.companieshouse.account.filing.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.http.ApiKeyHttpClient;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.environment.impl.EnvironmentReaderImpl;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class ApplicationConfiguration {
    @Value("${application.namespace}")
    private String applicationNameSpace;

    /**
     * Creates the logger used by the application.
     *
     * @return the logger
     */
    @Bean
    public Logger logger() {
        return LoggerFactory.getLogger(applicationNameSpace);
    }

    /**
     * Creates the rest template used for rest api calls
     *
     * @return the rest template
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * Creates the executor used for running asynchronous tasks
     *
     * @return the executor
     */
    @Bean
    public Executor executor() {
        return Executors.newWorkStealingPool();
    }

    /**
     * Creates the environment reader bean.
     *
     * @return The environment reader
     */
    @Bean
    public EnvironmentReader environmentReader() {
        return new EnvironmentReaderImpl();
    }
    @Bean
    public InternalApiClient internalApiClient(
            @Value("${api.base.path}") String apiBasePath,
            @Value("${internal.api.base.path}") String internalApiBasePath,
            @Value("${payments.api.base.path}") String paymentsApiBasePath,
            @Value("${internal.api.key}") String internalApiKey
    ) {
        ApiKeyHttpClient httpClient = new ApiKeyHttpClient(internalApiKey);
        InternalApiClient internalApiClient = new InternalApiClient(httpClient);

        internalApiClient.setBasePath(internalApiBasePath);
        internalApiClient.setBasePaymentsPath(paymentsApiBasePath);
        internalApiClient.setInternalBasePath(internalApiBasePath);

        return internalApiClient;
    }
}

