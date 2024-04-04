package uk.gov.companieshouse.accounts.filing.service.accounts;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.accounts.filing.model.AccountsFilingEntry;
import uk.gov.companieshouse.accounts.filing.model.types.AccountsType;
import uk.gov.companieshouse.accounts.filing.model.types.PackageType;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusError;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusResponse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

@Component
public class AccountsFilingValidator {

    /**
     * @param accountsFilingEntry - data which needs to be validated
     * @return ValidationStatusResponse - Contains the validation status of the
     *         accounts filing entry
     */
    public ValidationStatusResponse validateAccountsFilingEntry(AccountsFilingEntry accountsFilingEntry) {
        ValidationStatusResponse validationStatusResponse = new ValidationStatusResponse();
        List<ValidationStatusError> validationStatusErrors = new ArrayList<>();
        validatePackageType(accountsFilingEntry.getPackageType(), validationStatusErrors);
        validateAccountsType(accountsFilingEntry.getAccountsType(), validationStatusErrors);
        validateMadeUpDate(accountsFilingEntry.getMadeUpDate(), validationStatusErrors);
        validateFileId(accountsFilingEntry.getFileId(), validationStatusErrors);
        if (!validationStatusErrors.isEmpty()) {
            validationStatusResponse.setValid(false);
            validationStatusResponse
                    .setValidationStatusError(validationStatusErrors.toArray(new ValidationStatusError[0]));
        } else {
            validationStatusResponse.setValid(true);
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
    private void validatePackageType(PackageType packageType, List<ValidationStatusError> validationStatusErrors) {
        if (packageType == null) {
            setValidationError(validationStatusErrors, "PackageType", "Package type is null");
        } else if (!PackageType.UKSEF.equals(packageType)) {
            setValidationError(validationStatusErrors, "PackageType : " + packageType,
                    "Package type is not UKSEF");
        }
    }

    /**
     * This method validates whether the accounts type is a valid
     * 
     * @param accountsType           - accounts Type of the given accounts filing
     *                               entry
     * @param validationStatusErrors - List which holds the validation errors
     */
    private void validateAccountsType(String accountsType, List<ValidationStatusError> validationStatusErrors) {
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
    private boolean isValidAccountsType(String accountsType) {
        return !AccountsType.fromStemCode(accountsType).equals(AccountsType.UNKNOWN);
    }

    /**
     * This method validates the made up date is valid or not
     * 
     * @param madeUpDate             - made up date of the given accounts filing
     *                               entry
     * @param validationStatusErrors - List which holds the validation errors
     */
    private void validateMadeUpDate(String madeUpDate, List<ValidationStatusError> validationStatusErrors) {
        if (madeUpDate == null || madeUpDate.isBlank()) {
            setValidationError(validationStatusErrors, "MadeUpDate : " + madeUpDate,
                    "Made up date is null or blank");
            return;
        }
        if ("UNKNOWN".equals(madeUpDate)) {
            setValidationError(validationStatusErrors, "MadeUpDate : " + madeUpDate,
                    "Made up date is unknown");
            return;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(madeUpDate);
        } catch (ParseException parseException) {
            setValidationError(validationStatusErrors, "MadeUpDate : " + madeUpDate,
                    "Made up date is not in yyyy-MM-dd format");
        }
    }

    /**
     * This method validates the file id is a UUID
     * 
     * @param fileId                 - file id on the given accounts filing entry
     * @param validationStatusErrors - List which holds the validation errors
     */
    private void validateFileId(String fileId, List<ValidationStatusError> validationStatusErrors) {
        if (fileId == null || fileId.isBlank()) {
            setValidationError(validationStatusErrors, "FileId", "File ID is null or blank");
        } else {
            try {
                UUID uuid = UUID.fromString(fileId);
            } catch (IllegalArgumentException e) {
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
    private void setValidationError(List<ValidationStatusError> validationStatusErrors, String fieldName,
            String errorDescription) {
        ValidationStatusError validationStatusError = new ValidationStatusError();
        validationStatusError.setError(errorDescription);
        validationStatusError.setLocation(fieldName);
        validationStatusError.setType("ch:validation");
        validationStatusError.setLocationType("json-path");
        validationStatusErrors.add(validationStatusError);
    }
}