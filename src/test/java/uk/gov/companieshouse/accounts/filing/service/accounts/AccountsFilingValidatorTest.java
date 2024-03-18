package uk.gov.companieshouse.accounts.filing.service.accounts;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.accounts.filing.model.AccountsFilingEntry;
import uk.gov.companieshouse.accounts.filing.model.enums.PackageType;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AccountsFilingValidatorTest {

    AccountsFilingValidator filingValidator;

    ValidationStatusResponse validationStatusResponse;
    @BeforeEach
    void setUp() {
        filingValidator = new AccountsFilingValidator();
        validationStatusResponse = new ValidationStatusResponse();
    }


    @Test
    @DisplayName("Testing accounts filing data with null and invalid values")
    void testValidateAccountsFilingEntryForNullInvalidValues(){
        final var accountFilingId = "accountsFilingId";
        AccountsFilingEntry entry = new AccountsFilingEntry(accountFilingId);
        verifyValidation(entry,false, 4);

        entry.setPackageType(null);
        entry.setAccountsType(null);
        entry.setMadeUpDate(null);
        entry.setFileId(null);
        verifyValidation(entry,false, 4);

        entry.setPackageType(null);
        entry.setAccountsType("");
        entry.setMadeUpDate("");
        entry.setFileId("");
        verifyValidation(entry,false, 4);

        entry.setPackageType(PackageType.GROUP_PACKAGE_400);
        entry.setAccountsType("19");
        entry.setMadeUpDate("2021-06-31");
        entry.setFileId("9df3ddab-c199-467e-80d6-40405b1c824g");
        verifyValidation(entry,false, 4);
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
        verifyValidation(entry,true, 0);
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
        verifyValidation(entry,false, 1);

        entry.setPackageType(PackageType.GROUP_PACKAGE_400);
        verifyValidation(entry,false, 1);

        entry.setPackageType(PackageType.UKSEF);
        verifyValidation(entry,true, 1);
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
        verifyValidation(entry,false, 1);

        entry.setAccountsType("");
        verifyValidation(entry,false, 1);

        entry.setAccountsType("0");
        verifyValidation(entry,false, 1);

        entry.setAccountsType("19");
        verifyValidation(entry,false, 1);

        entry.setAccountsType("1");
        verifyValidation(entry,true, 0);

        entry.setAccountsType("18");
        verifyValidation(entry,true, 0);

        entry.setAccountsType("10");
        verifyValidation(entry,true, 0);
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
        verifyValidation(entry,false, 1);

        entry.setMadeUpDate("");
        verifyValidation(entry,false, 1);

        entry.setMadeUpDate("MadeUpDate");
        verifyValidation(entry,false, 1);

        entry.setMadeUpDate("2018-30-30");
        verifyValidation(entry,false, 1);

        entry.setMadeUpDate("06-06-2013");
        verifyValidation(entry,false, 1);

        entry.setMadeUpDate("2018-06-30");
        verifyValidation(entry,true, 0);
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
        verifyValidation(entry,false, 1);

        entry.setFileId("");
        verifyValidation(entry,false, 1);

        entry.setFileId("9df3ddab-c199-467e-80d6-40405b1c824G");
        verifyValidation(entry,false, 1);

        entry.setFileId("9df3ddab-c199-467e-80d6-40405b1c824a");
        verifyValidation(entry,true, 0);
    }

    void verifyValidation(AccountsFilingEntry entry, boolean expectedVaidationStatus, int expectedValidationErrorCount){
        validationStatusResponse = filingValidator.validateAccountsFilingEntry(entry);
        if(expectedVaidationStatus){
            assertTrue(validationStatusResponse.isValid());
            assertNull(validationStatusResponse.getValidationStatusError());
        }
        else{
            assertFalse(validationStatusResponse.isValid());
            assertNotNull(validationStatusResponse.getValidationStatusError());
            assertEquals(expectedValidationErrorCount, validationStatusResponse.getValidationStatusError().length);
        }
    }
}