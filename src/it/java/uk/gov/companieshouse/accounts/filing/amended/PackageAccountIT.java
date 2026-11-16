package uk.gov.companieshouse.accounts.filing.amended;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.noContent;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.patchRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.resetAllRequests;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;
import org.wiremock.spring.EnableWireMock;

import tools.jackson.databind.json.JsonMapper;
import uk.gov.companieshouse.accounts.filing.builder.AccountsFilingEntryBuilder;
import uk.gov.companieshouse.accounts.filing.builder.AccountsValidatorResultApiBuilder;
import uk.gov.companieshouse.accounts.filing.model.AccountsFilingEntry;
import uk.gov.companieshouse.accounts.filing.repository.AccountsFilingRepository;
import uk.gov.companieshouse.accounts.filing.service.api.ApiClientService;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.model.accountvalidator.AccountsValidatorDataApi;
import uk.gov.companieshouse.api.model.accountvalidator.AccountsValidatorStatusApi;
import uk.gov.companieshouse.api.model.accountvalidator.AccountsValidatorValidationStatusApi;
import uk.gov.companieshouse.api.model.felixvalidator.PackageTypeApi;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
@EnableWireMock
@AutoConfigureRestTestClient
class PackageAccountIT {
    private static final String VALIDATION_STATUS_URI = "/account-validator/validate/check/";
    private static final String URI_TO_FILE_STATUS = "/transactions/{transactionId}/accounts-filing/{accountsFilingId}/file/{fileId}/status";

    private static final String PRIVATE_TRANSACTION_URI = "/private/transactions/" + TRANSACTION_ID;

    private static final String TRANSACTION_ID = "123456-123456-123456";
    private static final String ACCOUNTS_FILING_ID = "0123456789abcdef01234567";
    private static final String FILE_ID = "01234567-1234-1234-1234-012345678910";
    private static final String COMPANY_NAME = "Example Company Limited";
    private static final String COMPANY_NUMBER = "01234567";
    private static final String INITIAL_MADE_UP_DATE = "1999-12-31";
    private static final String VALIDATOR_STATUS_NAME = "name";
    private static final String VALIDATOR_STATUS_OK = "OK";
    private static final String VALIDATOR_BALANCE_SHEET_DATE = "2000-01-01";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String JSON_CONTENT_TYPE = "application/json";
    private static final String AUTHORISED_TOKEN_PERMISSIONS_HEADER = "ERIC-Authorised-Token-Permissions";
    private static final String AUTHORISED_TOKEN_PERMISSIONS =
        "company_accounts=read company_package_accounts=read user_profile=read";

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:8.2.5-noble");
    
    @Autowired
    private RestTestClient client;

    @Autowired 
    private JsonMapper jsonMapper;

    @Autowired
    private MongoTemplate mongoTemplate;

    @MockitoSpyBean
    private AccountsFilingRepository accountsFilingRepository;

    @MockitoBean
    private ApiClientService apiClientService;

    @Autowired
    private InternalApiClient internalApiClient;

    @BeforeEach
    void setUp() {
        mongoTemplate.getDb().drop();
        resetAllRequests();
        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        stubFor(get(PRIVATE_TRANSACTION_URI)
            .willReturn(ok()
                .withHeader(CONTENT_TYPE_HEADER, JSON_CONTENT_TYPE)
                .withBody("{\"id\":\"" + TRANSACTION_ID + "\",\"resources\":{}}")));
        stubFor(patch(PRIVATE_TRANSACTION_URI).willReturn(noContent()));
    }

    private void verifyAllowedUpdate(final String accountType) {
        verify(1, getRequestedFor(urlEqualTo(PRIVATE_TRANSACTION_URI)));
        verify(1, patchRequestedFor(urlEqualTo(PRIVATE_TRANSACTION_URI)));
        AccountsFilingEntry savedEntry = accountsFilingRepository.findById(ACCOUNTS_FILING_ID).orElseThrow();
        assertEquals(accountType, savedEntry.getAccountsType());
        assertEquals(VALIDATOR_BALANCE_SHEET_DATE, savedEntry.getMadeUpDate());
    }

    @Nested
    @DisplayName("Allowed amended accounts")
    class AllowedAmendedAccounts {

        @ParameterizedTest(name = "Account Type {0}")
        @ValueSource(strings = {"00", "08"})
        @DisplayName("Test when the result status of file validation returns a (non-amended) account type should return 200")
        void testShouldReturn200_WhenAccountTypeUnder50IsReturned(final String accountType) {
            AccountsFilingEntry dbEntry = AccountsFilingEntryBuilder.builder()
                .withAccountsFilingId(ACCOUNTS_FILING_ID)
                .withFileId(FILE_ID)
                .withPackageType(PackageTypeApi.GROUP_PACKAGE_400)
                .withTransactionId(TRANSACTION_ID)
                .withCompanyNumber(COMPANY_NUMBER)
                .withCompanyName(COMPANY_NAME)
                .withMadeUpDate(INITIAL_MADE_UP_DATE)
                .build();

            accountsFilingRepository.insert(dbEntry);
            
            stubFor(get(VALIDATION_STATUS_URI + FILE_ID)
                .willReturn(ok()
                    .withHeader(CONTENT_TYPE_HEADER, JSON_CONTENT_TYPE)
                    .withBody(jsonMapper.writeValueAsString(
                        new AccountsValidatorStatusApi(
                            FILE_ID,
                            VALIDATOR_STATUS_NAME,
                            VALIDATOR_STATUS_OK,
                            AccountsValidatorResultApiBuilder.builder()
                                .withData(new AccountsValidatorDataApi(VALIDATOR_BALANCE_SHEET_DATE,
                                    accountType, COMPANY_NUMBER))
                            .withFileValidationStatusApi(AccountsValidatorValidationStatusApi.OK)
                            .build()
                        ))
                    )
                )
            );

            client.get().uri(
                URI_TO_FILE_STATUS,
                TRANSACTION_ID,
                ACCOUNTS_FILING_ID,
                FILE_ID
            )
            .header(AUTHORISED_TOKEN_PERMISSIONS_HEADER, AUTHORISED_TOKEN_PERMISSIONS)
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.SC_OK);
            verifyAllowedUpdate(accountType);
        }


        @ParameterizedTest(name = "Account Type {0}")
        @ValueSource(strings = {"51", "58"})
        @DisplayName("Test when the result status of file validation returns an amended account type" 
            + " should return 200 when amend accounts are allowed")
        void testShouldReturn200WhenAmendedAccountTypeAreAllowed(final String accountType) {

            AccountsFilingEntry dbEntry = AccountsFilingEntryBuilder.builder()
                .withAccountsFilingId(ACCOUNTS_FILING_ID)
                .withFileId(FILE_ID)
                .withPackageType(PackageTypeApi.CIC)
                .withTransactionId(TRANSACTION_ID)
                .withCompanyNumber(COMPANY_NUMBER)
                .withCompanyName(COMPANY_NAME)
                .withMadeUpDate(INITIAL_MADE_UP_DATE)
                .build();

            accountsFilingRepository.insert(dbEntry);

            stubFor(get(VALIDATION_STATUS_URI + FILE_ID)
                .willReturn(ok()
                    .withHeader(CONTENT_TYPE_HEADER, JSON_CONTENT_TYPE)
                    .withBody(jsonMapper.writeValueAsString(
                        new AccountsValidatorStatusApi(
                            FILE_ID,
                            VALIDATOR_STATUS_NAME,
                            VALIDATOR_STATUS_OK,
                            AccountsValidatorResultApiBuilder.builder()
                                .withData(new AccountsValidatorDataApi(VALIDATOR_BALANCE_SHEET_DATE,
                                    accountType, COMPANY_NUMBER))
                            .withFileValidationStatusApi(AccountsValidatorValidationStatusApi.OK)
                            .build()
                        ))
                    )
                )
            );

            client.get().uri(
                URI_TO_FILE_STATUS,
                TRANSACTION_ID,
                ACCOUNTS_FILING_ID,
                FILE_ID
            )
            .header(AUTHORISED_TOKEN_PERMISSIONS_HEADER, AUTHORISED_TOKEN_PERMISSIONS).exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.SC_OK);
            verifyAllowedUpdate(accountType);
        }
    }

    @Nested
    @DisplayName("Disallow amended accounts")
    @TestPropertySource(properties = {"feature.flag.amended.default-enable-amended-packages=false"})
    class DisallowAmendedAccounts {

        @ParameterizedTest(name = "Account Type {0}")
        @ValueSource(strings = {"00", "08"})
        @DisplayName("Test when the result status of file validation returns a (non-amended) account type should return 200")
        void testShouldReturn200WhenAccountTypeUnder50IsReturned(final String accountType) {
            AccountsFilingEntry dbEntry = AccountsFilingEntryBuilder.builder()
                .withAccountsFilingId(ACCOUNTS_FILING_ID)
                .withFileId(FILE_ID)
                .withPackageType(PackageTypeApi.GROUP_PACKAGE_400)
                .withTransactionId(TRANSACTION_ID)
                .withCompanyNumber(COMPANY_NUMBER)
                .withCompanyName(COMPANY_NAME)
                .withMadeUpDate(INITIAL_MADE_UP_DATE)
                .build();

            accountsFilingRepository.insert(dbEntry);
            
            stubFor(get(VALIDATION_STATUS_URI + FILE_ID)
                .willReturn(ok()
                    .withHeader(CONTENT_TYPE_HEADER, JSON_CONTENT_TYPE)
                    .withBody(jsonMapper.writeValueAsString(
                        new AccountsValidatorStatusApi(
                            FILE_ID,
                            VALIDATOR_STATUS_NAME,
                            VALIDATOR_STATUS_OK,
                            AccountsValidatorResultApiBuilder.builder()
                                .withData(new AccountsValidatorDataApi(VALIDATOR_BALANCE_SHEET_DATE,
                                    accountType, COMPANY_NUMBER))
                            .withFileValidationStatusApi(AccountsValidatorValidationStatusApi.OK)
                            .build()
                        ))
                    )
                )
            );

            client.get().uri(
                URI_TO_FILE_STATUS,
                TRANSACTION_ID,
                ACCOUNTS_FILING_ID,
                FILE_ID
            )
            .header(AUTHORISED_TOKEN_PERMISSIONS_HEADER, AUTHORISED_TOKEN_PERMISSIONS)
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.SC_OK);
            verifyAllowedUpdate(accountType);
        }

        @ParameterizedTest(name = "Account Type {0}")
        @ValueSource(strings = {"51", "58"})
        @DisplayName("Test when the result status of file validation returns an amended account type should return 503")
        void testShouldReturn503WhenAccountTypeOver50IsReturned(final String accountType) {

            AccountsFilingEntry dbEntry = AccountsFilingEntryBuilder.builder()
                .withAccountsFilingId(ACCOUNTS_FILING_ID)
                .withFileId(FILE_ID)
                .withPackageType(PackageTypeApi.GROUP_PACKAGE_400)
                .withTransactionId(TRANSACTION_ID)
                .withCompanyNumber(COMPANY_NUMBER)
                .withCompanyName(COMPANY_NAME)
                .withMadeUpDate(INITIAL_MADE_UP_DATE)
                .build();

            accountsFilingRepository.insert(dbEntry);

            stubFor(get(VALIDATION_STATUS_URI + FILE_ID)
                .willReturn(ok()
                    .withHeader(CONTENT_TYPE_HEADER, JSON_CONTENT_TYPE)
                    .withBody(jsonMapper.writeValueAsString(
                        new AccountsValidatorStatusApi(
                            FILE_ID,
                            VALIDATOR_STATUS_NAME,
                            VALIDATOR_STATUS_OK,
                            AccountsValidatorResultApiBuilder.builder()
                                .withData(new AccountsValidatorDataApi(VALIDATOR_BALANCE_SHEET_DATE,
                                    accountType, COMPANY_NUMBER))
                            .withFileValidationStatusApi(AccountsValidatorValidationStatusApi.OK)
                            .build()
                        ))
                    )
                )
            );

            client.get().uri(
                URI_TO_FILE_STATUS,
                TRANSACTION_ID,
                ACCOUNTS_FILING_ID,
                FILE_ID
            )
            .header(AUTHORISED_TOKEN_PERMISSIONS_HEADER, AUTHORISED_TOKEN_PERMISSIONS).exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.SC_SERVICE_UNAVAILABLE);

            verify(getRequestedFor(urlEqualTo(PRIVATE_TRANSACTION_URI)));
            verify(0, patchRequestedFor(urlEqualTo(PRIVATE_TRANSACTION_URI)));
            org.mockito.Mockito.verify(accountsFilingRepository, never()).save(any(AccountsFilingEntry.class));
            assertEquals(dbEntry, accountsFilingRepository.findById(ACCOUNTS_FILING_ID).orElseThrow());
        }
    }

}
