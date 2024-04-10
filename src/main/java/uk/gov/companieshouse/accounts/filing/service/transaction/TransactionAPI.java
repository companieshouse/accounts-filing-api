package uk.gov.companieshouse.accounts.filing.service.transaction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.gov.companieshouse.accounts.filing.service.api.ApiClientService;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.privatetransaction.request.PrivateTransactionGet;
import uk.gov.companieshouse.api.handler.privatetransaction.request.PrivateTransactionPatch;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.transaction.Transaction;

@Component
class TransactionAPI {

    private final ApiClientService apiClientService;

    private static final String PRIVATE_TRANSACTION_URL = "/private/transactions/";

    @Autowired
    public TransactionAPI(ApiClientService apiClientService) {
        this.apiClientService = apiClientService;
    }

    public ApiResponse<Transaction> get(final String transactionId)
            throws ApiErrorResponseException, URIValidationException {
        String path = PRIVATE_TRANSACTION_URL + transactionId;
        PrivateTransactionGet get = apiClientService.getInternalApiClient().privateTransaction().get(path);
        return get.execute();
    }

    public ApiResponse<Void> patch(final Transaction transaction)
            throws ApiErrorResponseException, URIValidationException {
        String path = PRIVATE_TRANSACTION_URL + transaction.getId();
        PrivateTransactionPatch patch = apiClientService.getInternalApiClient()
                .privateTransaction()
                .patch(path, transaction);
        return patch.execute();
    }
}
