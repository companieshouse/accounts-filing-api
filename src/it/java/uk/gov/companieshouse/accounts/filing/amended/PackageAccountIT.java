package uk.gov.companieshouse.accounts.filing.amended;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;

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
    private static final String TRANSACTION_ID = "1234-1234-1234";
    private static final String ACCOUNTS_FILING_ID = "accounts-filing-id";
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

    @Autowired
    private AccountsFilingRepository accountsFilingRepository;

    @BeforeEach
    void setUp() {
        mongoTemplate.getDb().drop();
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
        }


        @ParameterizedTest(name = "Account Type {0}")
        @ValueSource(strings = {"50", "58"})
        @DisplayName("Test when the result status of file validation returns an amended account type" 
            + " should return 200 when amend accounts are allowed")
        void testShouldReturn200WhenAmendedAccountTypeAreAllowed(final String accountType) {

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
            .isEqualTo(HttpStatus.SC_OK);
        }
    }

    @Nested
    @DisplayName("Disallow amended accounts")
    @TestPropertySource(properties = {"feature.flag.amended.enable-all-amended-packages=false"})
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
        }

        @ParameterizedTest(name = "Account Type {0}")
        @ValueSource(strings = {"50", "58"})
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


        }
    }

}
