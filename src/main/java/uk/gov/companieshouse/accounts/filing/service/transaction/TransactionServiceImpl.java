package uk.gov.companieshouse.accounts.filing.service.transaction;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import uk.gov.companieshouse.accounts.filing.exceptionhandler.ExternalServiceException;
import uk.gov.companieshouse.accounts.filing.exceptionhandler.ResponseException;
import uk.gov.companieshouse.accounts.filing.exceptionhandler.UriValidationException;
import uk.gov.companieshouse.accounts.filing.utils.mapping.ImmutableConverter;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;

@Component
public class TransactionServiceImpl implements TransactionService {

    private static final String EXPECTED = "expected";
    private static final String STATUS = "status";

    private final TransactionAPI transactionAPI;

    private final Logger logger;

    @Autowired
    public TransactionServiceImpl(final TransactionAPI transactionAPI, final Logger logger) {
        this.logger = logger;
        this.transactionAPI = transactionAPI;
    }

    @Override
    public Optional<Transaction> getTransaction(final String transactionId) throws NullPointerException {

        try {
            final ApiResponse<Transaction> response = transactionAPI.get(transactionId);
            return Optional.ofNullable(response.getData());
        } catch (ApiErrorResponseException e) {
            int statusCode = e.getStatusCode();
            final HttpStatus status = HttpStatus.resolve(statusCode);

            if (Objects.requireNonNull(status) == HttpStatus.NOT_FOUND) {
                return Optional.empty();
            } else {
                throw getTransactionThrowableExceptions(transactionId, status);
            }

        } catch (URIValidationException e) {
            throw new UriValidationException(e);
        }
    }

    @Override
    public void updateTransaction(final Transaction transaction) throws NullPointerException {
        final var message = "Unexpected response status when updating transaction.";
        final var externalIssueMessage = "External service issue blocked updating transaction.";
        try {
            final ApiResponse<Void> response = transactionAPI.patch(transaction);
            HttpStatus status = Objects.requireNonNull(HttpStatus.resolve(response.getStatusCode()));

            if (status != HttpStatus.NO_CONTENT) {
                logger.errorContext(transaction.getId(), message, null, ImmutableConverter.toMutableMap(Map.of(
                        EXPECTED, "204",
                        STATUS, status.value())));
                throw new ResponseException(message);
            }

        } catch (ApiErrorResponseException e) {
            final HttpStatus status = Objects.requireNonNull(HttpStatus.resolve(e.getStatusCode()));

            logger.errorContext(transaction.getId(), message, null, ImmutableConverter.toMutableMap(Map.of(
                    EXPECTED, "204",
                    STATUS, status.value())));

            if (status == HttpStatus.INTERNAL_SERVER_ERROR) {
                logger.errorContext(transaction.getId(), externalIssueMessage, null, new HashMap<>());
                throw new ExternalServiceException(externalIssueMessage);
            } else {
                throw new ResponseException(message);
            }

        } catch (URIValidationException e) {
            throw new UriValidationException(e);
        }
    }

    private RuntimeException getTransactionThrowableExceptions(final String transactionId, final HttpStatus status) {
        final var message = "Unexpected response status when getting transaction.";
        final var externalIssueMessage = "External issue blocked getting transaction.";
        logger.errorContext(transactionId, message, null, ImmutableConverter.toMutableMap(Map.of(
                EXPECTED, "200 or 404",
                STATUS, status)));

        if (status == HttpStatus.INTERNAL_SERVER_ERROR) {
            return new ExternalServiceException(externalIssueMessage);
        } else {
            return new ResponseException(message);
        }
    }

}