package uk.gov.companieshouse.accounts.filing.transformer;

import uk.gov.companieshouse.api.model.transaction.Transaction;

public interface TransactionTransformer {
    
    public void setupTransactionResources(final Transaction transaction, final String accountsFilingId, final String packageType);

}
