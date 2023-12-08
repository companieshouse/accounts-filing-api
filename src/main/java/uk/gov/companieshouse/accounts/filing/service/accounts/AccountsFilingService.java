package uk.gov.companieshouse.accounts.filing.service.accounts;

import uk.gov.companieshouse.accounts.filing.model.AccountsFilingEntry;
import uk.gov.companieshouse.api.model.accountvalidator.AccountsValidatorStatusApi;

public interface AccountsFilingService {

    public void savePackageType(AccountsFilingEntry accountsFilingEntry, String packageType);

    public void saveFileValidationResult(AccountsFilingEntry accountsFilingEntry, AccountsValidatorStatusApi accountsStatus);

    public AccountsFilingEntry getFilingEntry(String accountsFilingId);
    
}
