package uk.gov.companieshouse.accounts.filing.service.accounts;


import org.springframework.stereotype.Component;
import uk.gov.companieshouse.accounts.filing.model.AccountsFilingEntry;
import uk.gov.companieshouse.accounts.filing.model.enums.PackageType;
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
     * @return ValidationStatusResponse - Contains the validation status of the accounts filing entry
     */
    public ValidationStatusResponse validateAccountsFilingEntry(AccountsFilingEntry accountsFilingEntry){
        ValidationStatusResponse validationStatusResponse = new ValidationStatusResponse();
        List<ValidationStatusError> validationStatusErrors = new ArrayList<>();
        validatePackageType(accountsFilingEntry.getPackageType(), validationStatusErrors);
        validateAccountsType(accountsFilingEntry.getAccountsType(), validationStatusErrors);
        validateMadeUpDate(accountsFilingEntry.getMadeUpDate(), validationStatusErrors);
        validateFileId(accountsFilingEntry.getFileId(), validationStatusErrors);
        if(validationStatusErrors.size() > 0){
            validationStatusResponse.setValid(false);
            validationStatusResponse.setValidationStatusError(validationStatusErrors.toArray(new ValidationStatusError[0]));
        }
        else{
            validationStatusResponse.setValid(true);
        }
        return validationStatusResponse;
    }

    /**
     * This method checks the package type is UKSEF or not
     * @param packageType - package type of the given accounts filing entry
     * @param validationStatusErrors - List which holds the validation errors
     */
    private void validatePackageType(PackageType packageType, List<ValidationStatusError> validationStatusErrors){
        if(packageType == null){
            setValidationError(validationStatusErrors, "PackageType", "Package type is null");
        }
        else if(!PackageType.UKSEF.equals(packageType)){
            setValidationError(validationStatusErrors, "PackageType : " + packageType,
                    "Package type is not UKSEF");
        }
    }

    /**
     * This method validates whether the accounts type is in the range of 1 to 18
     * @param accountsType - accounts Type of the given accounts filing entry
     * @param validationStatusErrors - List which holds the validation errors
     */
    private void validateAccountsType(String accountsType, List<ValidationStatusError> validationStatusErrors){
        if(accountsType != null){
            try{
                if(!isValidAccountsType(Integer.parseInt(accountsType))){
                    setValidationError(validationStatusErrors, "AccountsType : " + accountsType,
                            "Accounts type is not between the range of 1 to 18");
                }
            }
            catch(NumberFormatException nfe){
                setValidationError(validationStatusErrors, "AccountsType : " + accountsType,
                        "Accounts type is not in integer format");
            }
        }
        else{
            setValidationError(validationStatusErrors, "AccountsType", "Accounts type is null");
        }
    }

    /**
     * @param accountsType - accounts Type of the given accounts filing entry
     * @return whether it's a valid account type
     */
    private boolean isValidAccountsType(int accountsType){
        return accountsType >= 1 && accountsType <= 18;
    }

    /**
     * This method validates the made up date is valid or not
     * @param madeUpDate - made up date of the given accounts filing entry
     * @param validationStatusErrors - List which holds the validation errors
     */
    private void validateMadeUpDate(String madeUpDate, List<ValidationStatusError> validationStatusErrors){
        if(madeUpDate != null){
            SimpleDateFormat dateFormat  = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            dateFormat.setLenient(false);
            try{
                dateFormat.parse(madeUpDate);
            }
            catch(ParseException parseException){
                setValidationError(validationStatusErrors, "MadeUpDate : " + madeUpDate,
                        "Made up date is not in yyyy-MM-dd format");
            }
        }
        else{
            setValidationError(validationStatusErrors, "MadeUpDate", "Made up date is null");
        }
    }


    /**
     * This method validates the file id is a UUID
     * @param fileId - file id on the given accounts filing entry
     * @param validationStatusErrors - List which holds the validation errors
     */
    private void validateFileId(String fileId, List<ValidationStatusError> validationStatusErrors){
        if(fileId == null){
            setValidationError(validationStatusErrors, "FileId", "File ID is null");
        }
        else{
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
     * @param fieldName - field name which is validated
     * @param errorDescription - description of the validation error
     */
    private void setValidationError(List<ValidationStatusError> validationStatusErrors, String fieldName, String errorDescription){
        ValidationStatusError validationStatusError = new ValidationStatusError();
        validationStatusError.setError(errorDescription);
        validationStatusError.setLocation(fieldName);
        validationStatusError.setType("ch:validation");
        validationStatusError.setLocationType("json-path");
        validationStatusErrors.add(validationStatusError);
    }
}