package uk.gov.companieshouse.accounts.filing.service.file.validation;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.companieshouse.accounts.filing.model.AccountsFilingRecord;
import uk.gov.companieshouse.accounts.filing.repository.AccountsFilingRepository;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.accountvalidator.PrivateAccountsValidatorResourceHandler;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.accountvalidator.AccountsValidatorDataApi;
import uk.gov.companieshouse.api.model.accountvalidator.AccountsValidatorResultApi;
import uk.gov.companieshouse.api.model.accountvalidator.AccountsValidatorStatusApi;
import uk.gov.companieshouse.api.model.accountvalidator.AccountsValidatorValidationStatusApi;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class AccountsValidationServiceTest {

    @Mock
    AccountsFilingRepository accountsFilingRepository;

    @Mock
    InternalApiClient internalApiClient;

    @Mock
    Logger logger;

    @Mock
    PrivateAccountsValidatorResourceHandler resourceHandler;

    @Mock
    ApiResponse<AccountsValidatorStatusApi> mockResponse;

    @Mock
    AccountsValidationAPI api;

    AccountsValidationService service;

    @BeforeEach
    void setUp(){
        service = new AccountsValidationService(
            logger, accountsFilingRepository, api);
    }

    @Test
    @DisplayName("Save an accounts validation results are save to the repository")
    void testSaveFileValidationResult() {
        String fileId = "aaaaaaaa-caaa-aaae-aaaa-111f4a118111";
        String accountStatus = "OK";
        String accountFilingId = "accountFilingId";
        String accountType = "accountType";
        String fileName = "fileName";
        String date = "01-01-2000";
        String registationNumber = "0";
        AccountsValidatorValidationStatusApi validationStatus = AccountsValidatorValidationStatusApi.OK;
        AccountsFilingRecord requestFilingStatus = AccountsFilingRecord.validateResult(accountFilingId, fileId, accountType);

        AccountsValidatorDataApi accountsValidatorStatusApi = createAccountsValidatorDataApi(date, accountType, registationNumber);
        AccountsValidatorStatusApi accountsValidatorStatus = createAccountsValidatorStatusApi(fileId, fileName, accountStatus, 
                                                                                              validationStatus, accountsValidatorStatusApi);

        when(accountsFilingRepository.save(requestFilingStatus)).thenReturn(requestFilingStatus);

        service.saveFileValidationResult(accountFilingId, accountsValidatorStatus);

        verify(accountsFilingRepository, times(1)).save(requestFilingStatus);
    }

    @Test
    @DisplayName("Call validation check and success return response")
    void testValidationStatusResult() throws ApiErrorResponseException, URIValidationException {

        String fileId = "fileId";

        AccountsValidatorStatusApi mockValidationStatus = mock(AccountsValidatorStatusApi.class);
        when(api.getValidationCheck(fileId)).thenReturn(mockResponse);
        when(mockResponse.getStatusCode()).thenReturn(200);
        when(mockResponse.getData()).thenReturn(mockValidationStatus);

        Optional<AccountsValidatorStatusApi> optionalResponse = service.validationStatusResult(fileId);

        assertThat(optionalResponse, is(Optional.of(mockValidationStatus)));

    }

    @Test
    @DisplayName("Call validation check and file is not found and returns empty optional")
    void testValidationStatusResultMissingFile() throws ApiErrorResponseException, URIValidationException {
        String fileId = "fileId";

        when(api.getValidationCheck(fileId)).thenReturn(mockResponse);
        when(mockResponse.getStatusCode()).thenReturn(404);

        Optional<AccountsValidatorStatusApi> optionalResponse = service.validationStatusResult(fileId);

        assertThat(optionalResponse, is(Optional.empty()));
    }

    @Test
    @DisplayName("Call validation check but external issue occurs")
    void testValidationStatusResultUnexpectedResponse() {
        String fileId = "fileId";
        
        when(api.getValidationCheck(fileId)).thenReturn(mockResponse);
        when(mockResponse.getStatusCode()).thenReturn(500);

        assertThrows(RuntimeException.class, () -> service.validationStatusResult(fileId));
    }

    private AccountsValidatorDataApi createAccountsValidatorDataApi(String date, String accountType, String requesteredNumber){
        return new AccountsValidatorDataApi(date, accountType, requesteredNumber);
    }

    private AccountsValidatorStatusApi createAccountsValidatorStatusApi(String fileId, String fileName, String status,
                                                                        AccountsValidatorValidationStatusApi validationStatus,
                                                                        AccountsValidatorDataApi accountsValidatorDataApi){
        AccountsValidatorResultApi accountsValidatorResultApi = new AccountsValidatorResultApi(accountsValidatorDataApi, validationStatus);
        return new AccountsValidatorStatusApi(fileId, fileName, status, accountsValidatorResultApi);
    }
    
}
