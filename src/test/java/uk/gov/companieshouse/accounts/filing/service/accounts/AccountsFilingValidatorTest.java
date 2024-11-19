package uk.gov.companieshouse.accounts.filing.service.accounts;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import uk.gov.companieshouse.accounts.filing.model.AccountsFilingEntry;
import uk.gov.companieshouse.api.model.felixvalidator.PackageTypeApi;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusResponse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Stream;

class AccountsFilingValidatorTest {

    AccountsFilingValidator filingValidator;

    ValidationStatusResponse validationStatusResponse;

    AccountsFilingEntry entry;

    String packageNullMessage = "Package type is null";
    String accountNullMessage = "Accounts type is null or blank";
    String madeUpDateNullMessage = "Made up date is null or blank";
    String fileIdMessageNullMessage = "File ID is null or blank";
    String accountNotValidType = "Accounts type is not a valid AccountsType";
    String dateWrongFormat = "Made up date is not in yyyy-MM-dd format";
    String fileIdNotUUID = "File ID is not a valid UUID";

    @BeforeEach
    void setUp() {
        filingValidator = new AccountsFilingValidator();
        validationStatusResponse = new ValidationStatusResponse();
        var accountFilingId = "accountsFilingId";
        entry = new AccountsFilingEntry(accountFilingId);
    }

    @Test
    @DisplayName("Testing accounts filing data with null values")
    void testValidateAccountsFilingEntryForNullValues() {
        entry.setPackageType(null);
        entry.setAccountsType(null);
        entry.setMadeUpDate(null);
        entry.setFileId(null);
        validateAccountsFilingEntry(entry);
        assertEquals(4, validationStatusResponse.getValidationStatusError().length);
        assertValidationFailedWithError(packageNullMessage);
        assertValidationFailedWithError(accountNullMessage);
        assertValidationFailedWithError(madeUpDateNullMessage);
        assertValidationFailedWithError(fileIdMessageNullMessage);
        assertNoErrorsAreNull();
        assertNoTypesAreNull();

    }

    @Test
    @DisplayName("Testing accounts filing data with null and blank values")
    void testValidateAccountsFilingEntryForNullAndBlankValues() {
        entry.setPackageType(null);
        entry.setAccountsType("");
        entry.setMadeUpDate("");
        entry.setFileId("");
        validateAccountsFilingEntry(entry);
        assertEquals(4, validationStatusResponse.getValidationStatusError().length);
        assertValidationFailedWithError(packageNullMessage);
        assertValidationFailedWithError(accountNullMessage);
        assertValidationFailedWithError(madeUpDateNullMessage);
        assertValidationFailedWithError(fileIdMessageNullMessage);
        assertNoErrorsAreNull();
        assertNoTypesAreNull();
    }

    @Test
    @DisplayName("Testing accounts filing data with invalid values")
    void testValidateAccountsFilingEntryForInvalidValues() {
        entry.setPackageType(PackageTypeApi.GROUP_PACKAGE_400);
        entry.setAccountsType("19");
        entry.setMadeUpDate("2021-06-31");
        entry.setFileId("9df3ddab-c199-467e-80d6-40405b1c824g");
        validateAccountsFilingEntry(entry);
        assertEquals(3, validationStatusResponse.getValidationStatusError().length);
        assertValidationFailedWithError(accountNotValidType);
        assertValidationFailedWithError(dateWrongFormat);
        assertValidationFailedWithError(fileIdNotUUID);
        assertNoErrorsAreNull();
        assertNoTypesAreNull();
    }

    @Test
    @DisplayName("Testing accounts filing data with valid values")
    void testValidateAccountsFilingEntryWithValidValues() {
        entry.setPackageType(PackageTypeApi.UKSEF);
        entry.setAccountsType("01");
        entry.setMadeUpDate("2018-06-30");
        entry.setFileId("9df3ddab-c199-467e-80d6-40405b1c824a");
        validateAccountsFilingEntry(entry);
        assertValidationSuccessful();
    }

    @Test
    @DisplayName("Testing accounts filing data with null package types")
    void testValidateAccountsFilingEntryWithNullPackageType() {
        entry.setAccountsType("01");
        entry.setMadeUpDate("2018-06-30");
        entry.setFileId("9df3ddab-c199-467e-80d6-40405b1c824a");

        entry.setPackageType(null);
        validateAccountsFilingEntry(entry);
        assertValidationFailedWithOneError(packageNullMessage);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "null,Accounts type is null or blank",
            "' ',Accounts type is null or blank",
            "0,Accounts type is not a valid AccountsType",
            "19,Accounts type is not a valid AccountsType",
            "10,Accounts type is not a valid AccountsType"
    }, nullValues = "null")
    @DisplayName("Testing accounts filing data fails with different accounts types")
    void testFailsValidateAccountsFilingEntryWithDifferentAccountsType(String accountType, String errorMessage) {
        entry.setPackageType(PackageTypeApi.UKSEF);
        entry.setMadeUpDate("2018-06-30");
        entry.setFileId("9df3ddab-c199-467e-80d6-40405b1c824a");

        entry.setAccountsType(accountType);
        validateAccountsFilingEntry(entry);
        assertValidationFailedWithOneError(
                errorMessage);
    }

    @ParameterizedTest
    @ValueSource(strings = { "01", "18" })
    @DisplayName("Testing accounts filing data passes with different accounts types")
    void testPassesValidateAccountsFilingEntryWithDifferentAccountsType(String accountType) {
        entry.setPackageType(PackageTypeApi.UKSEF);
        entry.setMadeUpDate("2018-06-30");
        entry.setFileId("9df3ddab-c199-467e-80d6-40405b1c824a");

        entry.setAccountsType(accountType);
        validateAccountsFilingEntry(entry);
        assertValidationSuccessful();
    }

    @ParameterizedTest
    @CsvSource(value = {
            "null,Made up date is null or blank",
            "'',Made up date is null or blank",
            "UNKNOWN,Made up date is unknown",
            "MadeUpDate,Made up date is not in yyyy-MM-dd format",
            "2018-30-30,Made up date is not in yyyy-MM-dd format",
            "06-06-2013,Made up date is not in yyyy-MM-dd format"
    }, nullValues = "null")
    @DisplayName("Testing failed accounts filing data with different made up dates")
    void testFailedValidateAccountsFilingEntryWithDifferentMadeUpDate(String madeUpDate, String errorMessage) {
        entry.setPackageType(PackageTypeApi.UKSEF);
        entry.setAccountsType("09");
        entry.setFileId("9df3ddab-c199-467e-80d6-40405b1c824a");

        entry.setMadeUpDate(madeUpDate);
        validateAccountsFilingEntry(entry);
        assertValidationFailedWithOneError(
                errorMessage);

    }

    @ParameterizedTest
    @CsvSource(value = {
            "UNKNOWN,Made up date is unknown",
            "MadeUpDate,Made up date is not in yyyy-MM-dd format",
            "2018-30-30,Made up date is not in yyyy-MM-dd format",
            "06-06-2013,Made up date is not in yyyy-MM-dd format"
    }, nullValues = "null")
    @DisplayName("Testing failed accounts filing data with different made up dates")
    void testFailedValidateAccountsFilingEntryWithDifferentMadeUpDateForOverseas(String madeUpDate, String errorMessage) {
        entry.setPackageType(PackageTypeApi.OVERSEAS);
        entry.setAccountsType("09");
        entry.setFileId("9df3ddab-c199-467e-80d6-40405b1c824a");

        entry.setMadeUpDate(madeUpDate);
        validateAccountsFilingEntry(entry);
        assertValidationCheckWithOneErrorForMUDForOverseas(
                errorMessage);
    }

    @Test
    @DisplayName("Testing passed accounts filing data with different made up dates")
    void testPassedMadeUpDate() {
        entry.setPackageType(PackageTypeApi.UKSEF);
        entry.setAccountsType("09");
        entry.setFileId("9df3ddab-c199-467e-80d6-40405b1c824a");
        entry.setMadeUpDate("2018-06-30");
        validateAccountsFilingEntry(entry);
        assertValidationSuccessful();
    }

    @ParameterizedTest
    @CsvSource(value = {
            "null,Made up date is null or blank",
            "'',Made up date is null or blank"
    }, nullValues = "null")
    @DisplayName("Testing passed accounts filing data for overseas with made up dates either to be null or blank")
    void testPassedMadeUpDateForOverseas(String madeUpDate) {
        entry.setPackageType(PackageTypeApi.OVERSEAS);
        entry.setAccountsType("09");
        entry.setFileId("9df3ddab-c199-467e-80d6-40405b1c824a");
        entry.setMadeUpDate(madeUpDate);
        validateAccountsFilingEntry(entry);
        assertValidationSuccessful();
    }

    @ParameterizedTest
    @CsvSource(value = {
            "null,File ID is null or blank",
            "'',File ID is null or blank",
            "9df3ddab-c199-467e-80d6-40405b1c824G,File ID is not a valid UUID"
    }, nullValues = "null")
    @DisplayName("Testing failed accounts filing data with different file ids")
    void testValidateAccountsFilingEntryWithDifferentFileId(String uuid, String errorMessage) {
        entry.setPackageType(PackageTypeApi.UKSEF);
        entry.setAccountsType("09");
        entry.setMadeUpDate("2018-06-30");

        entry.setFileId(uuid);
        validateAccountsFilingEntry(entry);
        assertValidationFailedWithOneError(
                errorMessage);
    }

    @Test
    @DisplayName("Testing accounts filing data with different file ids")
    void testPassedFileId() {
        entry.setPackageType(PackageTypeApi.UKSEF);
        entry.setAccountsType("09");
        entry.setMadeUpDate("2018-06-30");
        entry.setFileId("9df3ddab-c199-467e-80d6-40405b1c824a");
        validateAccountsFilingEntry(entry);
        assertValidationSuccessful();
    }

    void validateAccountsFilingEntry(AccountsFilingEntry entry) {
        validationStatusResponse = filingValidator.validateAccountsFilingEntry(entry);
    }

    void assertValidationFailedWithError(String expectedErrorMessage) {
        List<String> validationStatusErrors = Stream
                .of(validationStatusResponse.getValidationStatusError())
                .map(error -> error.getError())
                .filter(errorMessage -> errorMessage.equals(expectedErrorMessage))
                .toList();
        assertFalse(validationStatusErrors.isEmpty());
        assertFalse(validationStatusResponse.isValid());
        assertNotNull(validationStatusResponse.getValidationStatusError());
    }

    void assertNoErrorsAreNull() {
        assertThat("No errors are null",
                Stream.of(validationStatusResponse.getValidationStatusError())
                        .allMatch(e -> e.getError() != null));
    }

    void assertNoTypesAreNull() {
        assertThat("No types are null",
                Stream.of(validationStatusResponse.getValidationStatusError())
                        .allMatch(e -> e.getType() != null));
    }

    void assertValidationSuccessful() {
        assertTrue(validationStatusResponse.isValid());
        assertNull(validationStatusResponse.getValidationStatusError());
    }

    void assertValidationFailedWithOneError(String expectedErrorMessage) {
        assertFalse(validationStatusResponse.isValid());
        assertNotNull(validationStatusResponse.getValidationStatusError());
        assertEquals(1, validationStatusResponse.getValidationStatusError().length);
        ValidationStatusError validationStatusError = validationStatusResponse.getValidationStatusError()[0];
        assertEquals(expectedErrorMessage, validationStatusError.getError());
        assertNotNull(validationStatusError.getType());
    }
    void assertValidationCheckWithOneErrorForMUDForOverseas(String expectedErrorMessage) {
        assertFalse(validationStatusResponse.isValid());
        assertNotNull(validationStatusResponse.getValidationStatusError());
        assertEquals(1, validationStatusResponse.getValidationStatusError().length);
        ValidationStatusError validationStatusError = validationStatusResponse.getValidationStatusError()[0];
        assertEquals(expectedErrorMessage, validationStatusError.getError());
        assertNotNull(validationStatusError.getType());
    }
}