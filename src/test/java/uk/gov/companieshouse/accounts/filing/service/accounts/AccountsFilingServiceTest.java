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
import uk.gov.companieshouse.accounts.filing.model.enums.PackageType;
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

    @Test
    @DisplayName("Testing validation of accounts filing data")
    void testValidateAccountsFilingEntry(){
        final var accountFilingId = "accountsFilingId";
        AccountsFilingEntry entry = new AccountsFilingEntry(accountFilingId);
        assertFalse(service.validateAccountsFilingEntry(entry));

        entry.setPackageType(PackageType.UKSEF);
        entry.setAccountsType("1");
        entry.setMadeUpDate("2018-06-30");
        entry.setFileId("9df3ddab-c199-467e-80d6-40405b1c824a");
        assertTrue(service.validateAccountsFilingEntry(entry));
    }

    @Test
    @DisplayName("Testing accounts filing data with null values")
    void testValidateAccountsFilingEntryForNullValues(){
        final var accountFilingId = "accountsFilingId";
        AccountsFilingEntry entry = new AccountsFilingEntry(accountFilingId);
        assertFalse(service.validateAccountsFilingEntry(entry));

        entry.setPackageType(null);
        entry.setAccountsType(null);
        entry.setMadeUpDate(null);
        entry.setFileId(null);
        assertFalse(service.validateAccountsFilingEntry(entry));

        entry.setPackageType(null);
        entry.setAccountsType("");
        entry.setMadeUpDate("");
        entry.setFileId("");
        assertFalse(service.validateAccountsFilingEntry(entry));
    }

    @Test
    @DisplayName("Testing accounts filing data with valid values")
    void testValidateAccountsFilingEntryWithValidValues() {
        final var accountFilingId = "accountsFilingId";
        AccountsFilingEntry entry = new AccountsFilingEntry(accountFilingId);
        entry.setPackageType(PackageType.UKSEF);
        entry.setAccountsType("01");
        entry.setMadeUpDate("2018-06-30");
        entry.setFileId("9df3ddab-c199-467e-80d6-40405b1c824a");
        assertTrue(service.validateAccountsFilingEntry(entry));
    }

    @Test
    @DisplayName("Testing accounts filing data with different package types")
    void testValidateAccountsFilingEntryWithDifferentPackageType() {
        final var accountFilingId = "accountsFilingId";
        AccountsFilingEntry entry = new AccountsFilingEntry(accountFilingId);
        entry.setAccountsType("1");
        entry.setMadeUpDate("2018-06-30");
        entry.setFileId("9df3ddab-c199-467e-80d6-40405b1c824a");

        entry.setPackageType(null);
        assertFalse(service.validateAccountsFilingEntry(entry));

        entry.setPackageType(PackageType.GROUP_PACKAGE_400);
        assertFalse(service.validateAccountsFilingEntry(entry));

        entry.setPackageType(PackageType.UKSEF);
        assertTrue(service.validateAccountsFilingEntry(entry));
    }

    @Test
    @DisplayName("Testing accounts filing data with different accounts types")
    void testValidateAccountsFilingEntryWithDifferentAccountsType() {
        final var accountFilingId = "accountsFilingId";
        AccountsFilingEntry entry = new AccountsFilingEntry(accountFilingId);
        entry.setPackageType(PackageType.UKSEF);
        entry.setMadeUpDate("2018-06-30");
        entry.setFileId("9df3ddab-c199-467e-80d6-40405b1c824a");

        entry.setAccountsType(null);
        assertFalse(service.validateAccountsFilingEntry(entry));

        entry.setAccountsType("");
        assertFalse(service.validateAccountsFilingEntry(entry));

        entry.setAccountsType("0");
        assertFalse(service.validateAccountsFilingEntry(entry));

        entry.setAccountsType("19");
        assertFalse(service.validateAccountsFilingEntry(entry));

        entry.setAccountsType("1");
        assertTrue(service.validateAccountsFilingEntry(entry));

        entry.setAccountsType("18");
        assertTrue(service.validateAccountsFilingEntry(entry));

        entry.setAccountsType("10");
        assertTrue(service.validateAccountsFilingEntry(entry));
    }

    @Test
    @DisplayName("Testing accounts filing data with different made up dates")
    void testValidateAccountsFilingEntryWithDifferentMadeUpDate() {
        final var accountFilingId = "accountsFilingId";
        AccountsFilingEntry entry = new AccountsFilingEntry(accountFilingId);
        entry.setPackageType(PackageType.UKSEF);
        entry.setAccountsType("10");
        entry.setFileId("9df3ddab-c199-467e-80d6-40405b1c824a");

        entry.setMadeUpDate(null);
        assertFalse(service.validateAccountsFilingEntry(entry));

        entry.setMadeUpDate("");
        assertFalse(service.validateAccountsFilingEntry(entry));

        entry.setMadeUpDate("MadeUpDate");
        assertFalse(service.validateAccountsFilingEntry(entry));

        entry.setMadeUpDate("2018-30-30");
        assertFalse(service.validateAccountsFilingEntry(entry));

        entry.setMadeUpDate("06-06-2013");
        assertFalse(service.validateAccountsFilingEntry(entry));

        entry.setMadeUpDate("2018-06-30");
        assertTrue(service.validateAccountsFilingEntry(entry));
    }

    @Test
    @DisplayName("Testing accounts filing data with different file ids")
    void testValidateAccountsFilingEntryWithDifferentFileId() {
        final var accountFilingId = "accountsFilingId";
        AccountsFilingEntry entry = new AccountsFilingEntry(accountFilingId);
        entry.setPackageType(PackageType.UKSEF);
        entry.setAccountsType("10");
        entry.setMadeUpDate("2018-06-30");

        entry.setFileId(null);
        assertFalse(service.validateAccountsFilingEntry(entry));

        entry.setFileId("");
        assertFalse(service.validateAccountsFilingEntry(entry));

        entry.setFileId("9df3ddab-c199-467e-80d6-40405b1c824G");
        assertFalse(service.validateAccountsFilingEntry(entry));

        entry.setFileId("9df3ddab-c199-467e-80d6-40405b1c824a");
        assertTrue(service.validateAccountsFilingEntry(entry));
    }
}
