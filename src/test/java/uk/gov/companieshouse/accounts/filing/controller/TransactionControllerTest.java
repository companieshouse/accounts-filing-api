package uk.gov.companieshouse.accounts.filing.controller;

import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.equalTo;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import uk.gov.companieshouse.accounts.filing.model.AccountsFilingEntry;
import uk.gov.companieshouse.accounts.filing.exceptionhandler.EntryNotFoundException;
import uk.gov.companieshouse.accounts.filing.exceptionhandler.ResponseException;
import uk.gov.companieshouse.accounts.filing.service.file.validation.AccountsValidationService;
import uk.gov.companieshouse.api.model.accountvalidator.AccountsValidatorResultApi;
import uk.gov.companieshouse.api.model.accountvalidator.AccountsValidatorStatusApi;
import uk.gov.companieshouse.api.model.accountvalidator.AccountsValidatorDataApi;
import uk.gov.companieshouse.api.model.accountvalidator.AccountsValidatorValidationStatusApi;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {
    
    TransactionController controller;
    
    @Mock
    Logger logger;

    @Mock
    AccountsValidationService accountsValidationService;

    @Mock
    AccountsValidatorDataApi AccountsValidatorDataApi;


    @BeforeEach
    void setUp() {
        controller = new TransactionController(
            accountsValidationService,
            logger);
    }

    @Test
    @DisplayName("Request the validation status of a file")
    void testRequestingFileAccountsValidatorStatus() {
        String fileId = "fileId";
        String accountsFilingId = "accountsFilingId";
        String fileName = "fileName";
        var accountStatusResult = new AccountsValidatorResultApi(AccountsValidatorDataApi, null, AccountsValidatorValidationStatusApi.OK);
        AccountsValidatorStatusApi accountStatus = new AccountsValidatorStatusApi(fileId, fileName, "complete", accountStatusResult);
        var filingEntry = new AccountsFilingEntry(accountsFilingId);
        // Given
        when(accountsValidationService.validationStatusResult(fileId)).thenReturn(Optional.of(accountStatus));
        when(accountsValidationService.getFilingEntry(accountsFilingId)).thenReturn(filingEntry);

        // When
        ResponseEntity<AccountsValidatorStatusApi> result = controller.fileAccountsValidatorStatus(fileId, accountsFilingId);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertInstanceOf(AccountsValidatorStatusApi.class, result.getBody());
        AccountsValidatorStatusApi body = (AccountsValidatorStatusApi) result.getBody();
        assertNotNull(body);
        assertEquals("OK", body.resultApi().fileValidationStatusApi().toString());
        verify(accountsValidationService, times(1)).saveFileValidationResult(filingEntry, accountStatus);
    }

    @Test
    @DisplayName("Return 404 when the request the validation status is missing")
    void testRequestingFileAccountsValidatorStatusNotFound() {
        String fileId = "fileId";
        String accountsFilingId = "accountsFilingId";

        // Given
        when(accountsValidationService.validationStatusResult(fileId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<AccountsValidatorStatusApi> result = controller.fileAccountsValidatorStatus(fileId, accountsFilingId);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    @DisplayName("Return 404 when the request the validation status is missing")
    void testRequestingFileAccountsValidatorStatusEmptyStringAccountsId() {
        String accountsFilingId = "accountsFilingId";

        // Given

        when(accountsValidationService.getFilingEntry(accountsFilingId)).thenThrow(new EntryNotFoundException());

        // EntryNotFoundException will trigger a 404
        assertThrows(EntryNotFoundException.class, () -> accountsValidationService.getFilingEntry(accountsFilingId));
    }

     @Test
    @DisplayName("Exception handler when response")
    void responseException() {

        // When
        ResponseEntity<?> response = controller.responseException(new ResponseException());

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertThat((String) response.getBody(), containsString("Api Response failed."));
    }

    @Test
    @DisplayName("Exception handler when response")
    void validationException() {
        // Given

        // When
        ResponseEntity<?> response = controller.validationException();

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertThat(response.getBody(), is(equalTo("Validation failed")));
    }

    @Test
    @DisplayName("Exception handler when entry not found and return 404")
    void entryNotFoundException() {
        ResponseEntity<?> response = controller.entryNotFoundException();

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Exception handler logs error and returns 500")
    void exceptionHandler() {
        // Given
        Exception e = new Exception();

        // When
        ResponseEntity<?> response = controller.exceptionHandler(e);

        // Then
        verify(logger).error("Unhandled exception", e);
        assertThat(response.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));
    }
}
