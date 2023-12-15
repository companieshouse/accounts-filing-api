package uk.gov.companieshouse.accounts.filing.service.transaction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.gov.companieshouse.accounts.filing.exceptionhandler.ResponseException;
import uk.gov.companieshouse.accounts.filing.exceptionhandler.UriValidationException;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.privatetransaction.request.PrivateTransactionGet;
import uk.gov.companieshouse.api.handler.privatetransaction.request.PrivateTransactionPatch;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.transaction.Transaction;

@Component
class TransactionAPI {

    private final InternalApiClient internalApiClient;

    private static final String BASE_TRANSACTION_URL = "/private/transactions/";

    @Autowired
    public TransactionAPI(
            InternalApiClient internalApiClient) {
        this.internalApiClient = internalApiClient;
    }

    public ApiResponse<Transaction> get(final String transactionId) {
        String path = BASE_TRANSACTION_URL + transactionId;
        PrivateTransactionGet get = internalApiClient
                .privateTransaction()
                .get(path);
        try {
            return get.execute();
        } catch (Exception e) {
            throw handleCheckedExceptions(e);
        }
    }

    public ApiResponse<Void> patch(final Transaction transaction) {
        String path = BASE_TRANSACTION_URL + transaction.getId();
        PrivateTransactionPatch patch = internalApiClient
                .privateTransaction()
                .patch(path, transaction);
        try {
            return patch.execute();
        } catch (Exception e) {
            throw handleCheckedExceptions(e);
        }
    }

    /**
     * Method to wrap checked exceptions (currently only 2) within a
     * RuntimeException. This is done to -
     * 1. Prevent modification of upstream method signatures
     * 2. Be Lambda friendly (Lambda's can't throw checked exceptions)
     *
     * @param e checked exception to process
     * @return wrapped checked exception
     */
    private RuntimeException handleCheckedExceptions(Exception e) {
        return (e instanceof ApiErrorResponseException ? new ResponseException(e) : new UriValidationException(e));
    }
}
