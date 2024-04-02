package uk.gov.companieshouse.accounts.filing.service.accounts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusResponse;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class AccountsFilingServiceTest {

    AccountsFilingService service;

    @Mock
    AccountsFilingRepository accountsFilingRepository;

    @Mock
    AccountsFilingValidator accountsFilingValidator;

    @Mock
    Logger logger;

    @BeforeEach
    void setUp() {
        service = new AccountsFilingServiceImpl(accountsFilingRepository, accountsFilingValidator, logger);
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

    @Test
    @DisplayName("Testing validation of accounts filing data")
    void testValidateAccountsFilingEntry() {
        final var accountFilingId = "accountsFilingId";
        AccountsFilingEntry entry = new AccountsFilingEntry(accountFilingId);
        ValidationStatusResponse validationStatusResponse = new ValidationStatusResponse();

        validationStatusResponse.setValid(false);
        when(accountsFilingValidator.validateAccountsFilingEntry(entry)).thenReturn(validationStatusResponse);
        assertFalse((service.validateAccountsFilingEntry(entry)).isValid());

        validationStatusResponse.setValid(true);
        when(accountsFilingValidator.validateAccountsFilingEntry(entry)).thenReturn(validationStatusResponse);
        assertTrue((service.validateAccountsFilingEntry(entry)).isValid());
    }

    @Test
    @DisplayName("Testing whether IsTransactionAndAccountsFilingIdExists for true and false scenarios")
    void testIsTransactionAndAccountsFilingIdExists() {
        final var accountsFilingId = "accountsFilingId";
        final var transactionId = "transactionId";

        assertFalse(service.isTransactionAndAccountsFilingIdExists(transactionId, accountsFilingId));

        AccountsFilingEntry entry = new AccountsFilingEntry(accountsFilingId, null, null, null, transactionId, null, null);
        when(accountsFilingRepository.findById(accountsFilingId)).thenReturn(Optional.of(entry));
        assertFalse(service.isTransactionAndAccountsFilingIdExists("InvalidTransId", accountsFilingId));

        when(accountsFilingRepository.findById(accountsFilingId)).thenReturn(Optional.of(entry));
        assertTrue(service.isTransactionAndAccountsFilingIdExists(transactionId, accountsFilingId));
    }
}