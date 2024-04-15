package uk.gov.companieshouse.accounts.filing.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

import java.util.ArrayList;
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
import uk.gov.companieshouse.accounts.filing.service.costs.CostsService;
import uk.gov.companieshouse.accounts.filing.service.file.validation.AccountsValidationService;
import uk.gov.companieshouse.accounts.filing.service.transaction.TransactionService;
import uk.gov.companieshouse.accounts.filing.transformer.TransactionTransformer;
import uk.gov.companieshouse.api.model.accountvalidator.AccountsValidatorResultApi;
import uk.gov.companieshouse.api.model.accountvalidator.AccountsValidatorStatusApi;
import uk.gov.companieshouse.api.model.accountvalidator.AccountsValidatorDataApi;
import uk.gov.companieshouse.api.model.accountvalidator.AccountsValidatorValidationStatusApi;
import uk.gov.companieshouse.api.model.payment.CostsApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusResponse;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    TransactionController controller;

    AccountsFilingEntry accountsFilingEntry;

    private static final String transactionId = "transactionId";

    private static final String accountsFilingId = "accountsFilingId";

    @Mock
    Logger logger;

    @Mock
    AccountsValidationService accountsValidationService;

    @Mock
    AccountsFilingService accountsFilingService;

    @Mock
    TransactionService transactionService;

    @Mock
    CostsService costsService;

    @Mock
    AccountsValidatorDataApi AccountsValidatorDataApi;

    @Mock
    TransactionTransformer accountsFilingTransformer;

    CostsApi costs;


    ValidationStatusResponse validationStatusResponse;

    @BeforeEach
    void setUp() {
        controller = new TransactionController(logger, accountsValidationService, accountsFilingService,
                transactionService, accountsFilingTransformer, costsService);

        accountsFilingEntry = new AccountsFilingEntry(accountsFilingId, null, null, null,
                transactionId, null, null, null);
        validationStatusResponse = new ValidationStatusResponse();

        costs = new CostsApi();
        costs.setItems(new ArrayList<>());
    }

    @Test
    @DisplayName("Request the validation status of a file")
    void testRequestingFileAccountsValidatorStatus() {
        String fileId = "fileId";
        String accountsFilingId = "accountsFilingId";
        String fileName = "fileName";
        var accountStatusResult = new AccountsValidatorResultApi(AccountsValidatorDataApi, null,
                AccountsValidatorValidationStatusApi.OK);
        AccountsValidatorStatusApi accountStatus = new AccountsValidatorStatusApi(fileId, fileName, "complete",
                accountStatusResult);
        var filingEntry = new AccountsFilingEntry(accountsFilingId);
        // Given
        when(accountsValidationService.validationStatusResult(fileId)).thenReturn(Optional.of(accountStatus));
        when(accountsValidationService.getFilingEntry(accountsFilingId)).thenReturn(filingEntry);

        // When
        ResponseEntity<AccountsValidatorStatusApi> result = controller.fileAccountsValidatorStatus(fileId,
                accountsFilingId);

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
        ResponseEntity<AccountsValidatorStatusApi> result = controller.fileAccountsValidatorStatus(fileId,
                accountsFilingId);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    @DisplayName("Return 404 when the request the validation status is missing")
    void testRequestingFileAccountsValidatorStatusEmptyStringAccountsId() {
        String accountsFilingId = "accountsFilingId";
        String fileId = "fileId";

        ResponseEntity<?> response = controller.fileAccountsValidatorStatus(fileId, accountsFilingId);
        // trigger a 404
        assertThat(response.getStatusCode(), is(HttpStatus.NOT_FOUND));
    }

    @Test
    @DisplayName("Submit a package type and return a 204")
    void testSetPackageType() {
        String transactionId = "transactionId";
        String accountsFilingId = "accountsFilingId";
        AccountsPackageType packageType = new AccountsPackageType("Welsh");
        Transaction transaction = new Transaction();

        when(transactionService.getTransaction(transactionId)).thenReturn(Optional.of(transaction));

        ResponseEntity<String> responseEntity = controller.setPackageType(transactionId, accountsFilingId, packageType);

        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
    }

    @Test
    @DisplayName("Submit a package type and no matching transaction. Return a 404")
    void testSetPackageTypeMissingTransaction() {
        String transactionId = "transactionId";
        String accountsFilingId = "accountsFilingId";
        AccountsPackageType packageType = new AccountsPackageType("Welsh");

        ResponseEntity<String> responseEntity = controller.setPackageType(transactionId, accountsFilingId, packageType);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    @DisplayName("Submit a invalid package type. Throws an UriValidationException")
    void testSetPackageTypeWithInvalidPackageType() {
        String transactionId = "transactionId";
        String accountsFilingId = "accountsFilingId";
        AccountsPackageType invalidPackageType = new AccountsPackageType("Invalid");
        AccountsFilingEntry filingEntry = new AccountsFilingEntry(accountsFilingId);

        when(accountsFilingService.getFilingEntry(accountsFilingId)).thenReturn(filingEntry);
        doThrow(new UriValidationException()).when(accountsFilingService).savePackageType(filingEntry,
                invalidPackageType.type());

        assertThrows(UriValidationException.class,
                () -> controller.setPackageType(transactionId, accountsFilingId, invalidPackageType));
    }

    @Test
    @DisplayName("Submit a missing accountsFilingId. Throws an EntryNotFoundException")
    void testSetPackageTypeWithMissingAccountsFilingId() {
        String transactionId = "transactionId";
        String accountsFilingId = "noMatchingId";
        AccountsPackageType packageType = new AccountsPackageType("Welsh");

        doThrow(new EntryNotFoundException()).when(accountsFilingService).getFilingEntry(accountsFilingId);

        assertThrows(EntryNotFoundException.class,
                () -> controller.setPackageType(transactionId, accountsFilingId, packageType));
    }

    @Test
    @DisplayName("Exception handler when response")
    void responseException() {

        // When
        ResponseEntity<?> response = controller.exceptionHandler(new ResponseException());

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertThat((String) response.getBody(), containsString("Api Response failed."));
    }

    @Test
    @DisplayName("Exception handler when response")
    void validationException() {
        // Given

        // When
        ResponseEntity<?> response = controller.exceptionHandler(new UriValidationException());

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertThat(response.getBody(), is(equalTo("Validation failed")));
    }

    @Test
    @DisplayName("Exception handler when entry not found and return 404")
    void entryNotFoundException() {
        ResponseEntity<?> response = controller.exceptionHandler(new EntryNotFoundException());

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

    @Test
    @DisplayName("Exception handler deals with NullPointerExpection and returns 500")
    void exceptionHandlerNullPointerExpection() {
        NullPointerException e = new NullPointerException();

        ResponseEntity<?> response = controller.exceptionHandler(e);

        // Then
        verify(logger).error("Unhandled exception", e);
        assertThat(response.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    @DisplayName("Test validation status returns 200 with true")
    void testValidateAccountsFilingDataReturns200True() {
        // Given
        validationStatusResponse.setValid(true);
        when(accountsFilingService.getAccountsFilingEntryForIDAndTransaction(transactionId, accountsFilingId))
                .thenReturn(accountsFilingEntry);
        when(accountsFilingService.validateAccountsFilingEntry(accountsFilingEntry))
                .thenReturn(validationStatusResponse);

        // When
        ResponseEntity<?> validResult = controller.validateAccountsFilingData(transactionId, accountsFilingId);

        // Then
        Assertions.assertEquals(HttpStatus.OK, validResult.getStatusCode());
        Assertions.assertNotNull(validResult.getBody());
        Assertions.assertTrue(((ValidationStatusResponse) validResult.getBody()).isValid());
        verify(accountsFilingService, times(1)).validateAccountsFilingEntry(accountsFilingEntry);
    }

    @Test
    @DisplayName("Test validation status returns 200 with false")
    void testValidateAccountsFilingDataReturns200False() {
        // Given
        validationStatusResponse.setValid(false);
        when(accountsFilingService.getAccountsFilingEntryForIDAndTransaction(transactionId, accountsFilingId))
                .thenReturn(accountsFilingEntry);
        when(accountsFilingService.validateAccountsFilingEntry(accountsFilingEntry))
                .thenReturn(validationStatusResponse);

        // When
        ResponseEntity<?> inValidResult = controller.validateAccountsFilingData(transactionId, accountsFilingId);

        // Then
        Assertions.assertEquals(HttpStatus.OK, inValidResult.getStatusCode());
        Assertions.assertNotNull(inValidResult.getBody());
        Assertions.assertFalse(((ValidationStatusResponse) inValidResult.getBody()).isValid());
        verify(accountsFilingService, times(1)).validateAccountsFilingEntry(accountsFilingEntry);
    }

    @Test
    @DisplayName("Test validation status returns 404 for invalid transaction and filing id")
    void testValidateAccountsFilingDataReturns404() {
        // Given
        doThrow(new EntryNotFoundException("Accounts filing entry not found")).when(accountsFilingService)
                .getAccountsFilingEntryForIDAndTransaction(transactionId, accountsFilingId);

        // When
        ResponseEntity<?> inValidResult = controller.validateAccountsFilingData(transactionId, accountsFilingId);

        // Then
        Assertions.assertEquals(HttpStatus.NOT_FOUND, inValidResult.getStatusCode());
        Assertions.assertNull(inValidResult.getBody());
    }

    @Test
    @DisplayName("Test calculateCosts returns 200 with an empty cost array")
    void testCalculateCostsReturns200EmptyArray() {
        // Given
        when(accountsFilingService.getAccountsFilingEntryForIDAndTransaction(transactionId, accountsFilingId))
                .thenReturn(accountsFilingEntry);
        when(costsService.calculateCosts(accountsFilingEntry)).thenReturn(costs);
        // When
        ResponseEntity<?> validResult = controller.calculateCosts(transactionId, accountsFilingId);

        // Then
        Assertions.assertEquals(HttpStatus.OK, validResult.getStatusCode());
        Assertions.assertNotNull(validResult.getBody());
        Assertions.assertTrue(((CostsApi) validResult.getBody()).getItems().isEmpty());
    }

    @Test
    @DisplayName("Test calculateCosts returns 404 for transaction and filing id")
    void testCalculateCostsReturns404ForInvalidId() {
        // Given
        doThrow(new EntryNotFoundException("Accounts filing entry not found")).when(accountsFilingService)
                .getAccountsFilingEntryForIDAndTransaction(transactionId, accountsFilingId);
        // When
        ResponseEntity<?> inValidResult = controller.calculateCosts(transactionId, accountsFilingId);
        // Then
        Assertions.assertEquals(HttpStatus.NOT_FOUND, inValidResult.getStatusCode());
        Assertions.assertNull(inValidResult.getBody());
    }
}