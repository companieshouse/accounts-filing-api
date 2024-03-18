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

import uk.gov.companieshouse.accounts.filing.exceptionhandler.EntryNotFoundException;
import uk.gov.companieshouse.accounts.filing.exceptionhandler.ExternalServiceException;
import uk.gov.companieshouse.accounts.filing.exceptionhandler.InvalidStateException;
import uk.gov.companieshouse.accounts.filing.exceptionhandler.ResponseException;
import uk.gov.companieshouse.accounts.filing.model.AccountsFilingEntry;
import uk.gov.companieshouse.accounts.filing.repository.AccountsFilingRepository;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.charges.TransactionsApi;
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
    TransactionsApi transactionsApi;

    @Mock
    InternalApiClient internalApiClient;

    @Mock
    Logger logger;

    @Mock
    PrivateAccountsValidatorResourceHandler resourceHandler;

    @Mock
    ApiResponse<AccountsValidatorStatusApi> mockResponse;

    @Mock
    AccountsValidatorAPI api;

    AccountsValidationServiceImpl service;

    @BeforeEach
    void setUp(){
        service = new AccountsValidationServiceImpl(
            logger, accountsFilingRepository, api);
    }

    @Test
    @DisplayName("Get account filing entry from DB.")
    void testGetFilingEntry() {
        var accountFilingId = "accountFilingId";
        var filingEntry = new AccountsFilingEntry(accountFilingId);
        
        //when
        when(accountsFilingRepository.findById(accountFilingId)).thenReturn(Optional.of(filingEntry));

        //then
        var actualFilingEntry = service.getFilingEntry(accountFilingId);
        assertThat(actualFilingEntry, is(filingEntry));
        verify(accountsFilingRepository, times(1)).findById(accountFilingId);
    }

    @Test
    @DisplayName("Failed to find account filing entry from DB.")
    void testFailedToGetFilingEntry() {
        var accountFilingId = "accountFilingId";

        //when
        when(accountsFilingRepository.findById(accountFilingId)).thenReturn(Optional.empty());

        //then
        assertThrows(EntryNotFoundException.class, () -> service.getFilingEntry(accountFilingId));
        verify(accountsFilingRepository, times(1)).findById(accountFilingId);
    }

    @Test
    @DisplayName("Save an accounts validation results to the repository")
    void testSaveFileValidationResult() {
        String fileId = "aaaaaaaa-caaa-aaae-aaaa-111f4a118111";
        String accountStatus = "OK";
        String accountFilingId = "accountFilingId";
        String accountType = "accountType";
        String fileName = "fileName";
        String date = "01-01-2000";
        String registationNumber = "0";
        AccountsValidatorValidationStatusApi validationStatus = AccountsValidatorValidationStatusApi.OK;
        AccountsFilingEntry requestFilingStatus = new AccountsFilingEntry(accountFilingId, fileId, accountType,null, null, null, null);

        AccountsValidatorDataApi accountsValidatorStatusApi = createAccountsValidatorDataApi(date, accountType, registationNumber);
        AccountsValidatorStatusApi accountsValidatorStatus = createAccountsValidatorStatusApi(fileId, fileName, accountStatus, 
                                                                                              validationStatus, accountsValidatorStatusApi);

        when(accountsFilingRepository.save(requestFilingStatus)).thenReturn(requestFilingStatus);

        service.saveFileValidationResult(requestFilingStatus, accountsValidatorStatus);

        verify(accountsFilingRepository, times(1)).save(requestFilingStatus);
    }

    @Test
    @DisplayName("Fail to deal with incomplete accounts validation result")
    void testSaveFileValidationResultWithIncompleteAccountValidation() {

        String fileId = "aaaaaaaa-caaa-aaae-aaaa-111f4a118111";
        String accountStatus = "OK";
        String accountFilingId = "accountFilingId";
        String accountType = "accountType";
        String fileName = "fileName";
        AccountsValidatorValidationStatusApi validationStatus = AccountsValidatorValidationStatusApi.OK;
        AccountsFilingEntry requestFilingStatus = new AccountsFilingEntry(accountFilingId, fileId, accountType,null, null, null, null);

        AccountsValidatorDataApi accountsValidatorStatusApi = null;
        AccountsValidatorStatusApi accountsValidatorStatus = createAccountsValidatorStatusApi(fileId, fileName, accountStatus, 
                                                                                              validationStatus, accountsValidatorStatusApi);

        assertThrows(InvalidStateException.class, () -> service.saveFileValidationResult(requestFilingStatus, accountsValidatorStatus));
    }

    @Test
    @DisplayName("Call validation check and success return response")
    void testValidationStatusResult() throws ApiErrorResponseException, URIValidationException {

        String fileId = "fileId";

        AccountsValidatorStatusApi mockValidationStatus = mock(AccountsValidatorStatusApi.class);
        when(api.getValidationCheck(fileId)).thenReturn(mockResponse);
        when(mockResponse.getData()).thenReturn(mockValidationStatus);

        Optional<AccountsValidatorStatusApi> optionalResponse = service.validationStatusResult(fileId);

        assertThat(optionalResponse, is(Optional.of(mockValidationStatus)));

    }

    @Test
    @DisplayName("Call validation check and file is not found and returns empty optional")
    void testValidationStatusResultMissingFile() throws ApiErrorResponseException, URIValidationException {
        String fileId = "fileId";

        when(api.getValidationCheck(fileId)).thenReturn(mockResponse);

        Optional<AccountsValidatorStatusApi> optionalResponse = service.validationStatusResult(fileId);

        assertThat(optionalResponse, is(Optional.empty()));
    }

    @Test
    @DisplayName("Call validation check but 4xx request")
    void testValidationStatusResultUserIssue() throws ApiErrorResponseException, URIValidationException {
        String fileId = "fileId";
        ApiErrorResponseException responseExcetion = mock(ApiErrorResponseException.class);
        
        when(responseExcetion.getStatusCode()).thenReturn(401);
        when(api.getValidationCheck(fileId)).thenThrow(responseExcetion);

        assertThrows(ResponseException.class, () -> service.validationStatusResult(fileId));
    }

    @Test
    @DisplayName("Call validation check but external issue occurs")
    void testValidationStatusResultUnexpectedResponse() throws ApiErrorResponseException, URIValidationException {
        String fileId = "fileId";
        ApiErrorResponseException responseExcetion = mock(ApiErrorResponseException.class);
        
        when(responseExcetion.getStatusCode()).thenReturn(500);
        when(api.getValidationCheck(fileId)).thenThrow(responseExcetion);

        assertThrows(ExternalServiceException.class, () -> service.validationStatusResult(fileId));
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
