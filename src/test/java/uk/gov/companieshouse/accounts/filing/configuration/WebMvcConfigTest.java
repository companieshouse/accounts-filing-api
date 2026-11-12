package uk.gov.companieshouse.accounts.filing.configuration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.handler.MappedInterceptor;
import org.springframework.web.util.pattern.PathPatternParser;

import uk.gov.companieshouse.accounts.filing.controller.CompanyController;
import uk.gov.companieshouse.accounts.filing.controller.HealthCheckController;
import uk.gov.companieshouse.accounts.filing.controller.PrivateTransactionController;
import uk.gov.companieshouse.accounts.filing.controller.TransactionController;
import uk.gov.companieshouse.accounts.filing.interceptor.validation.AccountsFilingIdInterceptor;
import uk.gov.companieshouse.accounts.filing.interceptor.validation.FileIdInterceptor;
import uk.gov.companieshouse.accounts.filing.interceptor.validation.TransactionIdInterceptor;
import uk.gov.companieshouse.accounts.filing.mapper.FilingGeneratorMapper;
import uk.gov.companieshouse.accounts.filing.model.AccountsFilingEntry;
import uk.gov.companieshouse.accounts.filing.model.CompanyResponse;
import uk.gov.companieshouse.accounts.filing.security.LoggingInterceptor;
import uk.gov.companieshouse.accounts.filing.security.WebSecurityConfig;
import uk.gov.companieshouse.accounts.filing.service.accounts.AccountsFilingService;
import uk.gov.companieshouse.accounts.filing.service.company.CompanyService;
import uk.gov.companieshouse.accounts.filing.service.costs.CostsService;
import uk.gov.companieshouse.accounts.filing.service.file.validation.AccountsValidationService;
import uk.gov.companieshouse.accounts.filing.service.transaction.TransactionService;
import uk.gov.companieshouse.accounts.filing.transformer.TransactionTransformer;
import uk.gov.companieshouse.api.model.accountvalidator.AccountsValidatorStatusApi;
import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusResponse;
import uk.gov.companieshouse.logging.Logger;

@WebMvcTest({TransactionController.class, PrivateTransactionController.class,
        CompanyController.class, HealthCheckController.class})
@Import({WebMvcConfig.class, WebSecurityConfig.class, LoggingInterceptor.class,
        TransactionIdInterceptor.class, AccountsFilingIdInterceptor.class, FileIdInterceptor.class})
class WebMvcConfigTest {

    private static final String TRANSACTION_ID = "123456-123456-123456";
    private static final String FILING_ID = "0123456789abcdef01234567";
    private static final String FILE_ID = "01234567-1234-1234-1234-012345678910";
    private static final String PERMISSIONS_HEADER = "ERIC-Authorised-Token-Permissions";
    private static final String PERMISSIONS =
            "user_profile=read,update company_accounts=read,update";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebMvcConfig webMvcConfig;

    @MockitoBean
    private Logger logger;
    @MockitoBean
    private AccountsValidationService accountsValidationService;
    @MockitoBean
    private AccountsFilingService accountsFilingService;
    @MockitoBean
    private TransactionService transactionService;
    @MockitoBean
    private TransactionTransformer transactionTransformer;
    @MockitoBean
    private CostsService costsService;
    @MockitoBean
    private CompanyService companyService;
    @MockitoBean
    private FilingGeneratorMapper filingGeneratorMapper;
    @MockitoSpyBean
    private TransactionIdInterceptor transactionIdInterceptor;

    @ParameterizedTest(name = "{0}: transaction ID {1}")
    @MethodSource("malformedTransactionIds")
    void rejectsMalformedTransactionId(Route route, String transactionId) throws Exception {
        mockMvc.perform(authenticate(route, route.request(transactionId, FILING_ID, FILE_ID)))
                .andExpect(status().isBadRequest());

        // CompanyController also validates transaction IDs, so 400 alone cannot prove registration.
        verify(transactionIdInterceptor).preHandle(any(), any(), any());
        verifyNoDownstreamCalls();
    }

    private static Stream<Arguments> malformedTransactionIds() {
        return Stream.of(Route.values()).flatMap(route ->
                Stream.of("1234-1234-1234", "abcdef-123456-123456")
                        .map(id -> Arguments.of(route, id)));
    }

    @ParameterizedTest(name = "{0}: accounts-filing ID {1}")
    @MethodSource("malformedFilingIds")
    void rejectsMalformedAccountsFilingId(Route route, String filingId) throws Exception {
        mockMvc.perform(authenticate(route, route.request(TRANSACTION_ID, filingId, FILE_ID)))
                .andExpect(status().isBadRequest());
        verifyNoDownstreamCalls();
    }

    private static Stream<Arguments> malformedFilingIds() {
        return Stream.of(Route.values()).filter(route -> route != Route.COMPANY)
                .flatMap(route -> Stream.of("accounts-filing-id", "g123456789abcdef01234567")
                        .map(id -> Arguments.of(route, id)));
    }

    @ParameterizedTest
    @MethodSource("malformedFileIds")
    void rejectsMalformedFileId(String fileId) throws Exception {
        mockMvc.perform(authenticate(Route.FILE, Route.FILE.request(TRANSACTION_ID, FILING_ID, fileId)))
                .andExpect(status().isBadRequest());
        verifyNoDownstreamCalls();
    }

    private static Stream<String> malformedFileIds() {
        return Stream.of("file-id", "g1234567-1234-1234-1234-012345678910",
                "012345678901234567890123456789012345");
    }

    @ParameterizedTest
    @EnumSource(Route.class)
    void validIdsReachTheController(Route route) throws Exception {
        AccountsFilingEntry entry = new AccountsFilingEntry(FILING_ID);
        switch (route) {
            case PACKAGE -> {
                when(accountsFilingService.getFilingEntry(FILING_ID)).thenReturn(entry);
                when(transactionService.getTransaction(TRANSACTION_ID)).thenReturn(Optional.of(new Transaction()));
            }
            case VALIDATION -> {
                when(accountsFilingService.getAccountsFilingEntryForIDAndTransaction(TRANSACTION_ID, FILING_ID))
                        .thenReturn(entry);
                when(accountsFilingService.validateAccountsFilingEntry(entry))
                        .thenReturn(new ValidationStatusResponse());
            }
            case COSTS -> {
                when(accountsFilingService.getAccountsFilingEntryForIDAndTransaction(TRANSACTION_ID, FILING_ID))
                        .thenReturn(entry);
                when(costsService.calculateCosts(entry)).thenReturn(List.of());
            }
            case FILE -> {
                when(accountsValidationService.validationStatusResult(FILE_ID)).thenReturn(Optional.of(
                        new AccountsValidatorStatusApi(FILE_ID, "accounts.zip", "complete", null)));
                when(accountsValidationService.getFilingEntry(FILING_ID)).thenReturn(entry);
            }
            case COMPANY -> when(companyService.saveCompanyNumberAndTransactionId(
                    "01234567", TRANSACTION_ID, "Test Company")).thenReturn(new CompanyResponse(FILING_ID));
            case PRIVATE -> {
                when(accountsFilingService.getAccountsFilingEntryForIDAndTransaction(TRANSACTION_ID, FILING_ID))
                        .thenReturn(entry);
                when(filingGeneratorMapper.mapToFilingApi(entry)).thenReturn(new FilingApi());
            }
        }

        mockMvc.perform(authenticate(route, route.request(TRANSACTION_ID, FILING_ID, FILE_ID)))
                .andExpect(status().is(route == Route.PACKAGE ? 204 : 200));

        switch (route) {
            case PACKAGE -> {
                verify(accountsFilingService).savePackageType(entry, "Welsh");
                verify(transactionService).updateTransaction(any(Transaction.class));
            }
            case VALIDATION -> verify(accountsFilingService).validateAccountsFilingEntry(entry);
            case COSTS -> verify(costsService).calculateCosts(entry);
            case FILE -> verify(accountsValidationService).saveFileValidationResult(eq(entry), any());
            case COMPANY -> verify(companyService).saveCompanyNumberAndTransactionId(
                    "01234567", TRANSACTION_ID, "Test Company");
            case PRIVATE -> verify(filingGeneratorMapper).mapToFilingApi(entry);
        }
    }

    @ParameterizedTest
    @EnumSource(value = Route.class, names = {"COSTS", "PRIVATE"})
    void internalRoutesRejectMissingIdentity(Route route) throws Exception {
        mockMvc.perform(route.request(TRANSACTION_ID, FILING_ID, FILE_ID))
                .andExpect(status().isUnauthorized());
        verifyNoDownstreamCalls();
    }

    @ParameterizedTest
    @EnumSource(value = Route.class, names = {"COSTS", "PRIVATE"})
    void crudPermissionsDoNotReplaceInternalAuthentication(Route route) throws Exception {
        mockMvc.perform(route.request(TRANSACTION_ID, FILING_ID, FILE_ID)
                        .header(PERMISSIONS_HEADER, PERMISSIONS)
                        .header("ERIC-Identity", "test-user")
                        .header("ERIC-Identity-Type", "oauth2"))
                .andExpect(status().isForbidden());
        verifyNoDownstreamCalls();
    }

    @ParameterizedTest
    @EnumSource(value = Route.class, names = {"COSTS", "PRIVATE"})
    void internalRoutesRejectKeyWithoutInternalRole(Route route) throws Exception {
        mockMvc.perform(route.request(TRANSACTION_ID, FILING_ID, FILE_ID)
                        .header("ERIC-Identity", "test-key")
                        .header("ERIC-Identity-Type", "key"))
                .andExpect(status().isForbidden());
        verifyNoDownstreamCalls();
    }

    @ParameterizedTest
    @EnumSource(value = Route.class, names = {"COSTS", "PRIVATE"}, mode = EnumSource.Mode.EXCLUDE)
    void publicRoutesRequireCrudPermissions(Route route) throws Exception {
        mockMvc.perform(route.request(TRANSACTION_ID, FILING_ID, FILE_ID))
                .andExpect(status().isUnauthorized());
        verifyNoDownstreamCalls();
    }

    @Test
    void healthcheckDoesNotRequireAuthenticationOrIds() throws Exception {
        mockMvc.perform(get("/accounts-filing/healthcheck")).andExpect(status().isOk());
        verifyNoDownstreamCalls();
    }

    @Test
    void interceptorPatternsAreAbsoluteAndDoNotRequireAntFallback() {
        TestInterceptorRegistry registry = new TestInterceptorRegistry();
        webMvcConfig.addInterceptors(registry);
        for (Object interceptor : registry.registeredInterceptors()) {
            if (interceptor instanceof MappedInterceptor mapped) {
                assertValidPatterns(mapped.getIncludePathPatterns());
                assertValidPatterns(mapped.getExcludePathPatterns());
            }
        }
    }

    private static void assertValidPatterns(String[] patterns) {
        if (patterns != null) {
            for (String pattern : patterns) {
                assertTrue(pattern.startsWith("/"), pattern);
                assertDoesNotThrow(() -> PathPatternParser.defaultInstance.parse(pattern), pattern);
            }
        }
    }

    private static class TestInterceptorRegistry extends InterceptorRegistry {
        List<Object> registeredInterceptors() {
            return getInterceptors();
        }
    }

    private void verifyNoDownstreamCalls() {
        verifyNoInteractions(accountsValidationService, accountsFilingService, transactionService,
                transactionTransformer, costsService, companyService, filingGeneratorMapper);
    }

    private static MockHttpServletRequestBuilder authenticate(Route route, MockHttpServletRequestBuilder request) {
        if (route.internal) {
            return request.header("ERIC-Identity", "test-key")
                    .header("ERIC-Identity-Type", "key")
                    .header("ERIC-Authorised-Key-Roles", "*");
        }
        return request.header(PERMISSIONS_HEADER, PERMISSIONS);
    }

    private enum Route {
        PACKAGE(HttpMethod.PUT, "/{accountsFilingId}", false),
        VALIDATION(HttpMethod.GET, "/{accountsFilingId}/validation-status", false),
        COSTS(HttpMethod.GET, "/{accountsFilingId}/costs", true),
        FILE(HttpMethod.GET, "/{accountsFilingId}/file/{fileId}/status", false),
        COMPANY(HttpMethod.PUT, "/company/01234567/confirm", false),
        PRIVATE(HttpMethod.GET, "/{accountsFilingId}/filings", true);

        private final HttpMethod method;
        private final String suffix;
        private final boolean internal;

        Route(HttpMethod method, String suffix, boolean internal) {
            this.method = method;
            this.suffix = suffix;
            this.internal = internal;
        }

        MockHttpServletRequestBuilder request(String transactionId, String filingId, String fileId) {
            String path = (this == PRIVATE ? "/private" : "")
                    + "/transactions/{transactionId}/accounts-filing" + suffix;
            path = path.replace("{transactionId}", transactionId)
                    .replace("{accountsFilingId}", filingId).replace("{fileId}", fileId);
            MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(method, path);
            if (method == HttpMethod.PUT) {
                request.contentType(MediaType.APPLICATION_JSON).content(this == COMPANY
                        ? "{\"companyName\":\"Test Company\"}" : "{\"package_type\":\"Welsh\"}");
            }
            return request;
        }
    }
}
