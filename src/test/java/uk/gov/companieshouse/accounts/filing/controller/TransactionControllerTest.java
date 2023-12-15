package uk.gov.companieshouse.accounts.filing.controller;

import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
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
import uk.gov.companieshouse.accounts.filing.model.AccountsPackageType;
import uk.gov.companieshouse.accounts.filing.exceptionhandler.EntryNotFoundException;
import uk.gov.companieshouse.accounts.filing.exceptionhandler.ResponseException;
import uk.gov.companieshouse.accounts.filing.exceptionhandler.UriValidationException;
import uk.gov.companieshouse.accounts.filing.service.accounts.AccountsFilingService;
import uk.gov.companieshouse.accounts.filing.service.file.validation.AccountsValidationService;
import uk.gov.companieshouse.accounts.filing.service.transaction.TransactionService;
import uk.gov.companieshouse.accounts.filing.transformer.TransactionTransformer;
import uk.gov.companieshouse.api.model.accountvalidator.AccountsValidatorResultApi;
import uk.gov.companieshouse.api.model.accountvalidator.AccountsValidatorStatusApi;
import uk.gov.companieshouse.api.model.accountvalidator.AccountsValidatorDataApi;
import uk.gov.companieshouse.api.model.accountvalidator.AccountsValidatorValidationStatusApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {
    
    TransactionController controller;
    
    @Mock
    Logger logger;

    @Mock
    AccountsValidationService accountsValidationService;

    @Mock
    AccountsFilingService accountsFilingService;

    @Mock
    TransactionService transactionService;

    @Mock
    AccountsValidatorDataApi AccountsValidatorDataApi;

    @Mock
    TransactionTransformer accountsFilingTransformer;


    @BeforeEach
    void setUp() {
        controller = new TransactionController(
            accountsValidationService,
            accountsFilingService,
            transactionService,
            accountsFilingTransformer,
            logger);
    }

    @Test
    @DisplayName("Request the validation status of a file")
    void testRequestingFileAccountsValidatorStatus() {
        final String fileId = "fileId";
        final String accountsFilingId = "accountsFilingId";
        final String fileName = "fileName";
        final var accountStatusResult = new AccountsValidatorResultApi(AccountsValidatorDataApi, null, AccountsValidatorValidationStatusApi.OK);
        final AccountsValidatorStatusApi accountStatus = new AccountsValidatorStatusApi(fileId, fileName, "complete", accountStatusResult);
        final var filingEntry = new AccountsFilingEntry(accountsFilingId);
        // Given
        when(accountsValidationService.validationStatusResult(fileId)).thenReturn(Optional.of(accountStatus));
        when(accountsValidationService.getFilingEntry(accountsFilingId)).thenReturn(filingEntry);

        // When
        final ResponseEntity<AccountsValidatorStatusApi> result = controller.fileAccountsValidatorStatus(fileId, accountsFilingId);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertInstanceOf(AccountsValidatorStatusApi.class, result.getBody());
        final AccountsValidatorStatusApi body = (AccountsValidatorStatusApi) result.getBody();
        assertNotNull(body);
        assertEquals("OK", body.resultApi().fileValidationStatusApi().toString());
        verify(accountsValidationService, times(1)).saveFileValidationResult(filingEntry, accountStatus);
    }

    @Test
    @DisplayName("Return 404 when the request the validation status is missing")
    void testRequestingFileAccountsValidatorStatusNotFound() {
        final String fileId = "fileId";
        final String accountsFilingId = "accountsFilingId";

        // Given
        when(accountsValidationService.validationStatusResult(fileId)).thenReturn(Optional.empty());

        // When
        final ResponseEntity<AccountsValidatorStatusApi> result = controller.fileAccountsValidatorStatus(fileId, accountsFilingId);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    @DisplayName("Return 404 when the request the validation status is missing")
    void testRequestingFileAccountsValidatorStatusEmptyStringAccountsId() {
        final String accountsFilingId = "accountsFilingId";
        final String fileId = "fileId";

        ResponseEntity<?> response = controller.fileAccountsValidatorStatus(fileId, accountsFilingId);
        // trigger a 404
        assertThat(response.getStatusCode(), is(HttpStatus.NOT_FOUND));
    }

    @Test
    @DisplayName("Submit a package type and return a 204")
    void testSetPackageType() {
        final String transactionId = "transactionId";
        final String accountsFilingId = "accountsFilingId";
        final AccountsPackageType packageType = new AccountsPackageType("Welsh");
        final Transaction transaction = new Transaction();

        when(transactionService.getTransaction(transactionId)).thenReturn(Optional.of(transaction));

        ResponseEntity<String> responseEntity = controller.setPackageType(transactionId, accountsFilingId, packageType);

        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
    }

    @Test
    @DisplayName("Submit a package type and no matching transaction. Return a 404")
    void testSetPackageTypeMissingTransaction() {
        final String transactionId = "transactionId";
        final String accountsFilingId = "accountsFilingId";
        final AccountsPackageType packageType = new AccountsPackageType("Welsh");

        ResponseEntity<String> responseEntity = controller.setPackageType(transactionId, accountsFilingId, packageType);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    @DisplayName("Submit a invalid package type. Throws an UriValidationException")
    void testSetPackageTypeWithInvalidPackageType() {
        final String transactionId = "transactionId";
        final String accountsFilingId = "accountsFilingId";
        final AccountsPackageType invalidPackageType = new AccountsPackageType("Invalid");
        final AccountsFilingEntry filingEntry = new AccountsFilingEntry(accountsFilingId);

        when(accountsFilingService.getFilingEntry(accountsFilingId)).thenReturn(filingEntry);
        doThrow(new UriValidationException()).when(accountsFilingService).savePackageType(filingEntry, invalidPackageType.type());

        assertThrows(UriValidationException.class, () -> controller.setPackageType(transactionId, accountsFilingId, invalidPackageType));
    }

    @Test
    @DisplayName("Submit a missing accountsFilingId. Throws an EntryNotFoundException")
    void testSetPackageTypeWithMissingAccountsFilingId() {
        final String transactionId = "transactionId";
        final String accountsFilingId = "noMatchingId";
        final AccountsPackageType packageType = new AccountsPackageType("Welsh");

        doThrow(new EntryNotFoundException()).when(accountsFilingService).getFilingEntry(accountsFilingId);

        assertThrows(EntryNotFoundException.class, () -> controller.setPackageType(transactionId, accountsFilingId, packageType));
    }

    @Test
    @DisplayName("Exception handler when response")
    void responseException() {

        // When
        final ResponseEntity<?> response = controller.responseException(new ResponseException());

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertThat((String) response.getBody(), containsString("Api Response failed."));
    }

    @Test
    @DisplayName("Exception handler when response")
    void validationException() {
        // Given

        // When
        final ResponseEntity<?> response = controller.validationException();

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertThat(response.getBody(), is(equalTo("Validation failed")));
    }

    @Test
    @DisplayName("Exception handler when entry not found and return 404")
    void entryNotFoundException() {
        final ResponseEntity<?> response = controller.entryNotFoundException();

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Exception handler logs error and returns 500")
    void exceptionHandler() {
        // Given
        final Exception e = new Exception();

        // When
        final ResponseEntity<?> response = controller.exceptionHandler(e);

        // Then
        verify(logger).error("Unhandled exception", e);
        assertThat(response.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));
    }
}
