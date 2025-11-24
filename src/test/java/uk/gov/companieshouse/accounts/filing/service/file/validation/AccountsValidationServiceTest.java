package uk.gov.companieshouse.accounts.filing.service.file.validation;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.companieshouse.accounts.filing.exceptionhandler.EntryNotFoundException;
import uk.gov.companieshouse.accounts.filing.exceptionhandler.ExternalServiceException;
import uk.gov.companieshouse.accounts.filing.exceptionhandler.ResponseException;
import uk.gov.companieshouse.accounts.filing.model.AccountsFilingEntry;
import uk.gov.companieshouse.accounts.filing.repository.AccountsFilingRepository;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.accountvalidator.AccountsValidatorDataApi;
import uk.gov.companieshouse.api.model.accountvalidator.AccountsValidatorResultApi;
import uk.gov.companieshouse.api.model.accountvalidator.AccountsValidatorStatusApi;
import uk.gov.companieshouse.api.model.accountvalidator.AccountsValidatorValidationStatusApi;
import uk.gov.companieshouse.api.model.felixvalidator.PackageTypeApi;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class AccountsValidationServiceTest {

    @Mock
    AccountsFilingRepository accountsFilingRepository;

    @Mock
    Logger logger;

    @Mock
    ApiResponse<AccountsValidatorStatusApi> mockResponse;

    @Mock
    AccountsValidatorAPI api;

    @InjectMocks
    AccountsValidationServiceImpl service;

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
    void testSaveFileValidationResultOfIndependent() {

        String fileId = "aaaaaaaa-caaa-aaae-aaaa-111f4a118111";
        String accountStatus = "OK";
        String accountFilingId = "accountFilingId";
        String accountType = "accountType";
        String fileName = "fileName";
        String balanceSheetDate = "2021-01-30";
        String registrationNumber = "0";
        AccountsValidatorValidationStatusApi validationStatus = AccountsValidatorValidationStatusApi.OK;
        AccountsFilingEntry accountsFilingEntryRequest = new AccountsFilingEntry(accountFilingId);
        accountsFilingEntryRequest.setPackageType(PackageTypeApi.UKSEF);

        AccountsValidatorDataApi accountsValidatorStatusApi = createAccountsValidatorDataApi(balanceSheetDate, accountType, registrationNumber);
        AccountsValidatorStatusApi accountsValidatorStatus = createAccountsValidatorStatusApi(fileId, fileName, accountStatus, 
                                                                                              validationStatus, accountsValidatorStatusApi);

        service.saveFileValidationResult(accountsFilingEntryRequest, accountsValidatorStatus);
        Assertions.assertEquals("04", accountsFilingEntryRequest.getAccountsType());
        Assertions.assertEquals(fileId, accountsFilingEntryRequest.getFileId());
        Assertions.assertEquals(balanceSheetDate, accountsFilingEntryRequest.getMadeUpDate());

        verify(accountsFilingRepository, times(1)).save(accountsFilingEntryRequest);
    }

    @Test
    @DisplayName("Save an accounts validation results to the repository")
    void testSaveFileValidationResultOfFull() {

        String fileId = "aaaaaaaa-caaa-aaae-aaaa-111f4a118111";
        String accountStatus = "OK";
        String accountFilingId = "accountFilingId";
        String accountType = "accountType";
        String fileName = "fileName";
        String balanceSheetDate = "2021-01-30";
        String registrationNumber = "0";
        AccountsValidatorValidationStatusApi validationStatus = AccountsValidatorValidationStatusApi.OK;
        AccountsFilingEntry accountsFilingEntryRequest = new AccountsFilingEntry(accountFilingId);
        accountsFilingEntryRequest.setPackageType(PackageTypeApi.GROUP_PACKAGE_400);

        AccountsValidatorDataApi accountsValidatorStatusApi = createAccountsValidatorDataApi(balanceSheetDate, accountType, registrationNumber);
        AccountsValidatorStatusApi accountsValidatorStatus = createAccountsValidatorStatusApi(fileId, fileName, accountStatus,
                validationStatus, accountsValidatorStatusApi);

        service.saveFileValidationResult(accountsFilingEntryRequest, accountsValidatorStatus);
        Assertions.assertEquals(accountType, accountsFilingEntryRequest.getAccountsType());
        Assertions.assertEquals(fileId, accountsFilingEntryRequest.getFileId());
        Assertions.assertEquals(balanceSheetDate, accountsFilingEntryRequest.getMadeUpDate());

        verify(accountsFilingRepository, times(1)).save(accountsFilingEntryRequest);
    }

    @Test
    @DisplayName("Save an overseas accounts validation results to the repository")
    void testSaveOverseasFileValidationResult() {

        String fileId = "aaaaaaaa-caaa-aaae-aaaa-111f4a118111";
        String accountStatus = "OK";
        String accountFilingId = "accountFilingId";
        String accountType = "04";
        String fileName = "fileName";
        String registrationNumber = "0";
        AccountsValidatorValidationStatusApi validationStatus = AccountsValidatorValidationStatusApi.OK;
        AccountsFilingEntry accountsFilingEntryRequest = new AccountsFilingEntry(accountFilingId);
        accountsFilingEntryRequest.setPackageType(PackageTypeApi.OVERSEAS);

        AccountsValidatorDataApi accountsValidatorStatusApi = createAccountsValidatorDataApi(null, accountType, registrationNumber);
        AccountsValidatorStatusApi accountsValidatorStatus = createAccountsValidatorStatusApi(fileId, fileName, accountStatus,
                validationStatus, accountsValidatorStatusApi);

        service.saveFileValidationResult(accountsFilingEntryRequest, accountsValidatorStatus);
        Assertions.assertEquals(accountType, accountsFilingEntryRequest.getAccountsType());
        Assertions.assertEquals(fileId, accountsFilingEntryRequest.getFileId());
        Assertions.assertNull(accountsFilingEntryRequest.getMadeUpDate());

        verify(accountsFilingRepository, times(1)).save(accountsFilingEntryRequest);
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
        ApiErrorResponseException responseException = mock(ApiErrorResponseException.class);
        
        when(responseException.getStatusCode()).thenReturn(401);
        when(api.getValidationCheck(fileId)).thenThrow(responseException);

        assertThrows(ResponseException.class, () -> service.validationStatusResult(fileId));
    }

    @Test
    @DisplayName("Call validation check but external issue occurs")
    void testValidationStatusResultUnexpectedResponse() throws ApiErrorResponseException, URIValidationException {
        String fileId = "fileId";
        ApiErrorResponseException responseException = mock(ApiErrorResponseException.class);
        
        when(responseException.getStatusCode()).thenReturn(500);
        when(api.getValidationCheck(fileId)).thenThrow(responseException);

        assertThrows(ExternalServiceException.class, () -> service.validationStatusResult(fileId));
    }

    private AccountsValidatorDataApi createAccountsValidatorDataApi(String date, String accountType, String requestedNumber){
        return new AccountsValidatorDataApi(date, accountType, requestedNumber);
    }

    private AccountsValidatorStatusApi createAccountsValidatorStatusApi(String fileId, String fileName, String status,
                                                                        AccountsValidatorValidationStatusApi validationStatus,
                                                                        AccountsValidatorDataApi accountsValidatorDataApi){
        AccountsValidatorResultApi accountsValidatorResultApi = new AccountsValidatorResultApi(accountsValidatorDataApi, validationStatus);
        return new AccountsValidatorStatusApi(fileId, fileName, status, accountsValidatorResultApi);
    }
    
}
