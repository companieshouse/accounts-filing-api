package uk.gov.companieshouse.accounts.filing.service.accounts;

import uk.gov.companieshouse.accounts.filing.model.AccountsFilingEntry;
import uk.gov.companieshouse.api.model.transaction.Transaction;

public interface AccountsFilingService {

    public void savePackageType(AccountsFilingEntry accountsFilingEntry, String packageType);

    public AccountsFilingEntry getFilingEntry(String accountsFilingId);

    public void updateAccountsFilingTransaction(Transaction transaction, String accountsFilingId);
    
}
