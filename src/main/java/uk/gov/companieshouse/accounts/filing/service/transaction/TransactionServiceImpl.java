package uk.gov.companieshouse.accounts.filing.service.transaction;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import uk.gov.companieshouse.accounts.filing.exceptionhandler.ResponseException;
import uk.gov.companieshouse.accounts.filing.utils.mapping.ImmutableConverter;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;

@Component
public class TransactionServiceImpl implements TransactionService {

    private final TransactionAPI transactionAPI;

    private final Logger logger;

    @Autowired
    public TransactionServiceImpl(final TransactionAPI transactionAPI, final Logger logger) {
        this.logger = logger;
        this.transactionAPI = transactionAPI;
    }

    @Override
    public Optional<Transaction> getTransaction(final String transactionId) {
        final ApiResponse<Transaction> response = transactionAPI.get(transactionId);
        final HttpStatus status = HttpStatus.resolve(response.getStatusCode());

        switch (Objects.requireNonNull(status)) {
        case NOT_FOUND:
            return Optional.empty();
        case OK:
            return Optional.ofNullable(response.getData());
        default:
            final var message = "Unexpected response status when getting transaction.";
            logger.errorContext(transactionId, message, null, ImmutableConverter.toMutableMap(Map.of(
            "expected", "200 or 404",
            "status", response.getStatusCode()
            )));
            throw new ResponseException(message);
        }
    }

    @Override
    public void updateTransaction(final Transaction transaction) {

        final ApiResponse<Void> response = transactionAPI.patch(transaction);
        final HttpStatus status = HttpStatus.resolve(response.getStatusCode());
        
        if(status != HttpStatus.NO_CONTENT){
            final var message = "Unexpected response status when updating transaction.";
            logger.errorContext(transaction.getId(), message, null, ImmutableConverter.toMutableMap(Map.of(
            "expected", "204",
            "status", response.getStatusCode()
            )));
            throw new ResponseException(message);
        }
    }
    
}
