package uk.gov.companieshouse.accounts.filing.service.accounts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.companieshouse.accounts.filing.exceptionhandler.EntryNotFoundException;
import uk.gov.companieshouse.accounts.filing.exceptionhandler.UriValidationException;
import uk.gov.companieshouse.accounts.filing.model.AccountsFilingEntry;
import uk.gov.companieshouse.accounts.filing.repository.AccountsFilingRepository;
import uk.gov.companieshouse.accounts.filing.service.transaction.TransactionService;
import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class AccountsFilingServiceTest {

    private static final String RESOURCE_KIND = "accounts-filing-api";

    AccountsFilingService service;

    @Mock
    TransactionService transactionService;

    @Mock
    AccountsFilingRepository accountsFilingRepository;

    @Mock
    Logger logger;

    @BeforeEach
    void setUp() {
        service = new AccountsFilingServiceImpl(
            accountsFilingRepository,
            transactionService,
            logger);
    }

    @Test
    void testGetFilingEntry() {
        final var accountFilingId = "accountsFilingId";
        final AccountsFilingEntry entry = new AccountsFilingEntry(accountFilingId);


        when(accountsFilingRepository.findById(accountFilingId)).thenReturn(Optional.of(entry));

        AccountsFilingEntry returnEntry = service.getFilingEntry(accountFilingId);

        assertEquals(entry, returnEntry);
        verify(accountsFilingRepository, times(1)).findById(accountFilingId);

    }

    @Test
    void testGetFilingEntryEmptyReturnFromDB() {
        final var accountFilingId = "accountsFilingId";
        
        when(accountsFilingRepository.findById(accountFilingId)).thenReturn(Optional.empty());

        assertThrows(EntryNotFoundException.class, () -> service.getFilingEntry(accountFilingId));
    }

    @Test
    void testSavePackageType() {
        final var accountsFilingId = "accountsFilingId";
        AccountsFilingEntry entry = new AccountsFilingEntry(accountsFilingId);

        service.savePackageType(entry, "UKSEF");

        verify(accountsFilingRepository, times(1)).save(entry);
    }

    @Test
    void testSavePackageTypeBadPackageType() {
        final var accountsFilingId = "accountsFilingId";
        AccountsFilingEntry entry = new AccountsFilingEntry(accountsFilingId);

        assertThrows(UriValidationException.class, () -> service.savePackageType(entry, "BAD TYPE"));
    }

    @Test
    void testUpdateAccountsFilingTransaction() {
        final var transaction = new Transaction();
        final var transactionId = "transactionId";
        final var accountFilingId = "accountFilingId";
        final var uri = "/transactions/" + transactionId + "/account-filing/" + accountFilingId;
        // date in the past.
        final var localDateTime = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
        
        final Map<String, String> links = Map.of(
            "costs", uri+"/costs",
            "resource", uri,
            "validation_status", uri+"/validation-status"
            );
            
        transaction.setId(transactionId);
        transaction.setResources(new HashMap<>());

        //then
        service.updateAccountsFilingTransaction(transaction, accountFilingId);
        final Resource resourceResult = transaction.getResources().get(uri);

        verify(transactionService, times(1)).updateTransaction(transaction);
        assertEquals(1, transaction.getResources().size());
        assertTrue(transaction.getResources().containsKey(uri));
        assertEquals(RESOURCE_KIND, resourceResult.getKind());
        assertEquals(3, resourceResult.getLinks().size());
        assertEquals(links, resourceResult.getLinks());
        assertNotEquals(resourceResult.getUpdatedAt(), localDateTime);
    }

    @Test
    void testUpdateAccountsFilingTransactionWithInvalidParams() {
        final Transaction transaction = new Transaction();
        final String accountFilingId = null;

        assertThrows(IllegalArgumentException.class, () -> service.updateAccountsFilingTransaction(transaction, accountFilingId));
        verify(logger, times(2)).error(contains("can not be null or blank"));
    }
}
