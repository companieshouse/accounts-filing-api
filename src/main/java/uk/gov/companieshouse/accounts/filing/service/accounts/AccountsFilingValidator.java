package uk.gov.companieshouse.accounts.filing.service.accounts;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.accounts.filing.model.AccountsFilingEntry;
import uk.gov.companieshouse.accounts.filing.model.types.AccountsType;
import uk.gov.companieshouse.api.model.felixvalidator.PackageTypeApi;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusResponse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import static uk.gov.companieshouse.api.model.felixvalidator.PackageTypeApi.OVERSEAS;

@Component
public class AccountsFilingValidator {

    /**
     * @param accountsFilingEntry - data which needs to be validated
     * @return ValidationStatusResponse - Contains the validation status of the
     *         accounts filing entry
     */
    public ValidationStatusResponse validateAccountsFilingEntry(final AccountsFilingEntry accountsFilingEntry) {
        final ValidationStatusResponse validationStatusResponse = new ValidationStatusResponse();
        final List<ValidationStatusError> validationStatusErrors = new ArrayList<>();

        validatePackageType(accountsFilingEntry.getPackageType(), validationStatusErrors);
        validateAccountsType(accountsFilingEntry.getAccountsType(), validationStatusErrors);
        if(PackageTypeApi.OVERSEAS == accountsFilingEntry.getPackageType()){
            validateMadeUpDateForOverseas(accountsFilingEntry.getMadeUpDate(), validationStatusErrors);
        }else{
            validateMadeUpDate(accountsFilingEntry.getMadeUpDate(), validationStatusErrors);
        }
        validateFileId(accountsFilingEntry.getFileId(), validationStatusErrors);

        final boolean passedValidation = validationStatusErrors.isEmpty();
        validationStatusResponse.setValid(passedValidation);
        if (!passedValidation) {
            validationStatusResponse
                    .setValidationStatusError(validationStatusErrors.toArray(new ValidationStatusError[0]));
        }

        return validationStatusResponse;
    }

    /**
     * This method checks the package type is UKSEF or not
     * 
     * @param packageType            - package type of the given accounts filing
     *                               entry
     * @param validationStatusErrors - List which holds the validation errors
     */
    private void validatePackageType(final PackageTypeApi packageType,
            final List<ValidationStatusError> validationStatusErrors) {
        if (packageType == null) {
            setValidationError(validationStatusErrors, "PackageType", "Package type is null");
        }
    }

    /**
     * This method validates whether the accounts type is a valid
     * 
     * @param accountsType           - accounts Type of the given accounts filing
     *                               entry
     * @param validationStatusErrors - List which holds the validation errors
     */
    private void validateAccountsType(final String accountsType,
            final List<ValidationStatusError> validationStatusErrors) {
        if (accountsType == null || accountsType.isBlank()) {
            setValidationError(validationStatusErrors, "AccountsType", "Accounts type is null or blank");
            return;
        }
        if (!isValidAccountsType(accountsType)) {
            setValidationError(validationStatusErrors, "AccountsType : " + accountsType,
                    "Accounts type is not a valid AccountsType");
        }
    }

    /**
     * @param accountsType - accounts Type of the given accounts filing entry
     * @return whether it's a valid account type
     */
    private boolean isValidAccountsType(final String accountsType) {
        return !AccountsType.fromStemCode(accountsType).equals(AccountsType.UNKNOWN);
    }

    /**
     * This method validates the made up date is valid or not
     * 
     * @param madeUpDate             - made up date of the given accounts filing
     *                               entry
     * @param validationStatusErrors - List which holds the validation errors
     */
    private void validateMadeUpDate(final String madeUpDate, final List<ValidationStatusError> validationStatusErrors) {

        final String madeUpDateMessage = "MadeUpDate : " + madeUpDate;

        if (madeUpDate == null || madeUpDate.isBlank()) {
            setValidationError(validationStatusErrors, madeUpDateMessage,
                    "Made up date is null or blank");
            return;
        }
        if ("UNKNOWN".equals(madeUpDate)) {
            setValidationError(validationStatusErrors, madeUpDateMessage,
                    "Made up date is unknown");
            return;
        }
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(madeUpDate);
        } catch (final ParseException parseException) {
            setValidationError(validationStatusErrors, madeUpDateMessage,
                    "Made up date is not in yyyy-MM-dd format");
        }
    }
    /**
     * This method validates the made up date is valid or not for overseas
     *
     * @param madeUpDate             - made up date of the given accounts filing
     *                               entry
     * @param validationStatusErrors - List which holds the validation errors
     */
    private void validateMadeUpDateForOverseas(final String madeUpDate, final List<ValidationStatusError> validationStatusErrors) {

        final String madeUpDateMessage = "MadeUpDate : " + madeUpDate;

        if (madeUpDate == null || madeUpDate.isBlank()) {
            return;
        }

        if ("UNKNOWN".equals(madeUpDate)) {
            setValidationError(validationStatusErrors, madeUpDateMessage,
                    "Made up date is unknown");
            return;
        }
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(madeUpDate);
        } catch (final ParseException parseException) {
            setValidationError(validationStatusErrors, madeUpDateMessage,
                    "Made up date is not in yyyy-MM-dd format");
        }
    }

    /**
     * This method validates the file id is a UUID
     * 
     * @param fileId                 - file id on the given accounts filing entry
     * @param validationStatusErrors - List which holds the validation errors
     */
    private void validateFileId(final String fileId, final List<ValidationStatusError> validationStatusErrors) {
        if (fileId == null || fileId.isBlank()) {
            setValidationError(validationStatusErrors, "FileId", "File ID is null or blank");
        } else {
            try {
                UUID.fromString(fileId);
            } catch (final IllegalArgumentException e) {
                setValidationError(validationStatusErrors, "FileId : " + fileId,
                        "File ID is not a valid UUID");
            }
        }
    }

    /**
     * @param validationStatusErrors - List which holds the validation errors
     * @param fieldName              - field name which is validated
     * @param errorDescription       - description of the validation error
     */
    private void setValidationError(final List<ValidationStatusError> validationStatusErrors, final String fieldName,
            final String errorDescription) {
        final ValidationStatusError validationStatusError = new ValidationStatusError();
        validationStatusError.setError(errorDescription);
        validationStatusError.setLocation(fieldName);
        validationStatusError.setType("ch:validation");
        validationStatusError.setLocationType("json-path");
        validationStatusErrors.add(validationStatusError);
    }
}