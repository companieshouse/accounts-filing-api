package uk.gov.companieshouse.accounts.filing.service.transaction;

import java.util.Optional;

import uk.gov.companieshouse.api.model.transaction.Transaction;

public interface TransactionService {
    
    public Optional<Transaction> getTransaction(final String transactionId);
    
    public void updateTransaction(final Transaction transaction);

    public void updateTransactionWithPackagetype(final Transaction transaction, final String accountFilingId, final String packageType);
}
