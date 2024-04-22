package uk.gov.companieshouse.accounts.filing.service.accounts;

import uk.gov.companieshouse.accounts.filing.exceptionhandler.UriValidationException;
import uk.gov.companieshouse.accounts.filing.model.AccountsFilingEntry;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusResponse;

public interface AccountsFilingService {

    public void savePackageType(AccountsFilingEntry accountsFilingEntry, String packageType) throws UriValidationException;

    public AccountsFilingEntry getFilingEntry(String accountsFilingId);

    public ValidationStatusResponse validateAccountsFilingEntry(AccountsFilingEntry accountsFilingEntry);

    public AccountsFilingEntry getAccountsFilingEntryForIDAndTransaction(String transactionId, String accountsFilingId);
}
