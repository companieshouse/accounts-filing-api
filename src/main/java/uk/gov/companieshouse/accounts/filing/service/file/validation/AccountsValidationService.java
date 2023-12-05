package uk.gov.companieshouse.accounts.filing.service.file.validation;

import uk.gov.companieshouse.accounts.filing.model.AccountsFilingEntry;
import uk.gov.companieshouse.api.model.accountvalidator.AccountsValidatorStatusApi;

import java.util.Optional;

public interface AccountsValidationService {
    
    public Optional<AccountsValidatorStatusApi> validationStatusResult(String fileId);

    public void saveFileValidationResult(AccountsFilingEntry accountsFilingEntry, AccountsValidatorStatusApi accountStatus);

    public AccountsFilingEntry getFilingEntry(String accountsFilingId);

}
