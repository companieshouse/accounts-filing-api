package uk.gov.companieshouse.accounts.filing.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import uk.gov.companieshouse.accounts.filing.exceptionhandler.EntryNotFoundException;
import uk.gov.companieshouse.accounts.filing.mapper.FilingGeneratorMapper;
import uk.gov.companieshouse.accounts.filing.model.AccountsFilingEntry;
import uk.gov.companieshouse.accounts.filing.service.accounts.AccountsFilingService;
import uk.gov.companieshouse.accounts.filing.service.transaction.TransactionService;
import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class PrivateTransactionControllerTest {

    @Mock
    AccountsFilingService accountsFilingService;

    @Mock
    Logger logger;

    @Mock
    FilingGeneratorMapper filingGeneratorMapper;

    @Mock
    TransactionService transactionService;

    PrivateTransactionController controller;

    private final static String transactionId = "123456-123456-123456";

    private final static String accountFilingId = "accountFilingId";

    @Mock
    AccountsFilingEntry accountsFilingEntry;

    @BeforeEach
    void beforeEach() {
        controller = new PrivateTransactionController(accountsFilingService, filingGeneratorMapper, transactionService, logger);
    }

    @Test
    @DisplayName("Test with Unknown accountsFilingId")
    void testGetFilingApiEntryWithUnknownAccountsFilingId() {
        when(accountsFilingService.getFilingEntry(accountFilingId)).thenThrow(EntryNotFoundException.class);
        ResponseEntity<FilingApi> result = controller.getFilingApiEntry(transactionId, accountFilingId);
        assertEquals(HttpStatusCode.valueOf(404), result.getStatusCode());
    }

    @Test
    @DisplayName("Test with Unknown transactionId")
    void testGetFilingApiEntryWithUnknownTransactionId() {
        when(accountsFilingService.getFilingEntry(accountFilingId)).thenReturn(new AccountsFilingEntry(accountFilingId));
        when(transactionService.getTransaction(transactionId)).thenReturn(Optional.empty());
        ResponseEntity<FilingApi> result = controller.getFilingApiEntry(transactionId, accountFilingId);
        assertEquals(HttpStatusCode.valueOf(404), result.getStatusCode());
    }

    @Test
    @DisplayName("Test with accountFilingEntry's transactionId not matching input transactionId")
    void testGetFilingApiEntryWithMismatchIds() {

        when(accountsFilingService.getFilingEntry(accountFilingId)).thenReturn(accountsFilingEntry);
        when(transactionService.getTransaction(transactionId)).thenReturn(Optional.of(new Transaction()));
        when(accountsFilingEntry.getTransactionId()).thenReturn("not the right Id");
        ResponseEntity<FilingApi> result = controller.getFilingApiEntry(transactionId, accountFilingId);
        assertEquals(HttpStatusCode.valueOf(404), result.getStatusCode());
    }

    @Test
    @DisplayName("Test with accountFilingEntry's transactionId is null")
    void testGetFilingApiEntryWithAccountFilingEntryTransactionIdNull() {

        when(accountsFilingService.getFilingEntry(accountFilingId)).thenReturn(accountsFilingEntry);
        when(transactionService.getTransaction(transactionId)).thenReturn(Optional.of(new Transaction()));
        when(accountsFilingEntry.getTransactionId()).thenReturn(null);
        ResponseEntity<FilingApi> result = controller.getFilingApiEntry(transactionId, accountFilingId);
        assertEquals(HttpStatusCode.valueOf(404), result.getStatusCode());
    }

    @Test
    @DisplayName("Test with valid inputs")
    void testGetFilingApi() {
        when(accountsFilingService.getFilingEntry(accountFilingId)).thenReturn(accountsFilingEntry);
        when(transactionService.getTransaction(transactionId)).thenReturn(Optional.of(new Transaction()));
        when(accountsFilingEntry.getTransactionId()).thenReturn(transactionId);
        ResponseEntity<FilingApi> result = controller.getFilingApiEntry(transactionId, accountFilingId);
        assertEquals(HttpStatusCode.valueOf(200), result.getStatusCode());
    }

}
