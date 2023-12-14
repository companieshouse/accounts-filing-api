package uk.gov.companieshouse.accounts.filing.service.accounts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import uk.gov.companieshouse.accounts.filing.exceptionhandler.UriValidationException;
import uk.gov.companieshouse.accounts.filing.model.AccountsFilingEntry;
import uk.gov.companieshouse.accounts.filing.repository.AccountsFilingRepository;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class AccountsFilingServiceTest {

    AccountsFilingService service;

    @Mock
    AccountsFilingRepository accountsFilingRepository;

    @Mock
    Logger logger;

    @BeforeEach
    void setUp() {
        service = new AccountsFilingServiceImpl(
            accountsFilingRepository,
            logger);
    }

    @Test
    @DisplayName("Get Filing Entry")
    void testGetFilingEntry() {
        final var accountFilingId = "accountsFilingId";
        final AccountsFilingEntry entry = new AccountsFilingEntry(accountFilingId);


        when(accountsFilingRepository.findById(accountFilingId)).thenReturn(Optional.of(entry));

        AccountsFilingEntry returnEntry = service.getFilingEntry(accountFilingId);

        assertEquals(entry, returnEntry);
        verify(accountsFilingRepository, times(1)).findById(accountFilingId);

    }

    @Test
    @DisplayName("Failed to get filing entry because it does not exist.")
    void testGetFilingEntryEmptyReturnFromDB() {
        final var accountFilingId = "accountsFilingId";
        
        when(accountsFilingRepository.findById(accountFilingId)).thenReturn(Optional.empty());

        assertThrows(EntryNotFoundException.class, () -> service.getFilingEntry(accountFilingId));
    }

    @Test
    @DisplayName("Save package type to existing entry")
    void testSavePackageType() {
        final var accountsFilingId = "accountsFilingId";
        AccountsFilingEntry entry = new AccountsFilingEntry(accountsFilingId);

        service.savePackageType(entry, "UKSEF");

        verify(accountsFilingRepository, times(1)).save(entry);
    }

    @Test
    @DisplayName("Failed to save invalid package type to existing entry")
    void testSavePackageTypeBadPackageType() {
        final var accountsFilingId = "accountsFilingId";
        AccountsFilingEntry entry = new AccountsFilingEntry(accountsFilingId);

        assertThrows(UriValidationException.class, () -> service.savePackageType(entry, "BAD TYPE"));
    }
}
