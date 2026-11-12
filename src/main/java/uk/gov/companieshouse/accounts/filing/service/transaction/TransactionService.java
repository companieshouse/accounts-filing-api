package uk.gov.companieshouse.accounts.filing.service.transaction;

import java.util.Optional;

import uk.gov.companieshouse.api.model.transaction.Transaction;

public interface TransactionService {
    
    public Optional<Transaction> getTransaction(final String transactionId);
    
    public void updateTransaction(final Transaction transaction);

    /**
     * Retrieves the transaction, failing if it does not exist.
     *
     * @throws uk.gov.companieshouse.accounts.filing.exceptionhandler.EntryNotFoundException if the transaction is missing
     */
    public Transaction getRequiredTransaction(final String transactionId, final String accountsFilingId);

    /**
     * Applies the accounts filing resources to an already-resolved transaction and saves it.
     *
     * @param transaction the transaction previously returned by {@link #getRequiredTransaction}
     * @param accountsType the validated accounts type, or null to withhold the costs link during initial setup
     */
    public void updateTransactionResources(final Transaction transaction,
                                           final String accountsFilingId,
                                           final String packageType,
                                           final String accountsType);

}
