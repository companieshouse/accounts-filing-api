package uk.gov.companieshouse.accounts.filing.service.accounts;

import uk.gov.companieshouse.accounts.filing.model.AccountsFilingEntry;

public interface AccountsFilingService {

    public void savePackageType(AccountsFilingEntry accountsFilingEntry, String packageType);

    public AccountsFilingEntry getFilingEntry(String accountsFilingId);

    public boolean validateAccountsFilingEntry(AccountsFilingEntry accountsFilingEntry);
    
}
