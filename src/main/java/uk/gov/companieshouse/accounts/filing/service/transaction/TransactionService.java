package uk.gov.companieshouse.accounts.filing.service.transaction;

import java.util.Optional;

import uk.gov.companieshouse.api.model.transaction.Transaction;

public interface TransactionService {
    
    public Optional<Transaction> getTransaction(final String transactionId);
    
    public void updateTransaction(final Transaction transaction);

    /**
     * Updates the transaction resources for an accounts filing when the transaction exists.
     *
     * @param transactionId the identifier of the transaction
     * @param accountsFilingId the identifier of the accounts filing
     * @param packageType the package type to apply
     * @param accountsType the validated accounts type, or null to withhold the costs link during initial setup
     * @return the transaction when found, or an empty optional otherwise
     */
    public Optional<Transaction> updateTransactionResources(final String transactionId,
                                                            final String accountsFilingId,
                                                            final String packageType,
                                                            final String accountsType);

}
