package uk.gov.companieshouse.accounts.filing.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;


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
import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class PrivateTransactionControllerTest {

    @Mock
    AccountsFilingService accountsFilingService;

    @Mock
    Logger logger;

    @Mock
    FilingGeneratorMapper filingGeneratorMapper;

    PrivateTransactionController controller;

    private final static String transactionId = "123456-123456-123456";

    private final static String accountsFilingId = "accountFilingId";

    @Mock
    AccountsFilingEntry accountsFilingEntry;

    @BeforeEach
    void beforeEach() {
        controller = new PrivateTransactionController(accountsFilingService, filingGeneratorMapper, logger);
    }

    @Test
    @DisplayName("Test with accountFilingEntry's transactionId not matching input transactionId")
    void testGetFilingApiEntryWithMismatchIds() {

        when(accountsFilingService
        .getAccountsFilingEntryForIDAndTransaction(accountsFilingId, transactionId)).thenThrow(EntryNotFoundException.class);
        ResponseEntity<FilingApi> result = controller.getFilingApiEntry(transactionId, accountsFilingId);
        assertEquals(HttpStatusCode.valueOf(404), result.getStatusCode());
    }

    @Test
    @DisplayName("Test with valid inputs")
    void testGetFilingApi() {
        when(accountsFilingService
        .getAccountsFilingEntryForIDAndTransaction(accountsFilingId, transactionId)).thenReturn(accountsFilingEntry);
        ResponseEntity<FilingApi> result = controller.getFilingApiEntry(transactionId, accountsFilingId);
        assertEquals(HttpStatusCode.valueOf(200), result.getStatusCode());
    }

}
