package uk.gov.companieshouse.accounts.filing.service.transaction;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import uk.gov.companieshouse.accounts.filing.exceptionhandler.ResponseException;
import uk.gov.companieshouse.accounts.filing.utils.mapping.ImmutableConverter;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;

@Component
public class TransactionServiceImpl implements TransactionService {

    private static final String RESOURCE_KIND = "accounts-filing-api";

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

    @Override
    public void updateTransactionWithPackagetype(final Transaction transaction, final String accountsFilingId, final String packageType) {

        checkUriVariablesAreNullOrBlank(accountsFilingId, packageType);
        
        final var uri = String.format("/transactions/%s/account-filing/%s", transaction.getId(), accountsFilingId);
        final Resource resource = new Resource();
        resource.setKind(RESOURCE_KIND);
        resource.setUpdatedAt(LocalDateTime.now());
        resource.setLinks(Collections.emptyMap());
        transaction.getResources().put(uri, resource);
        updateTransaction(transaction);
    }

    private void checkUriVariablesAreNullOrBlank(String ...variables){
        var invalidVariables = Stream.of(variables)
            .filter(variable -> (variable == null || variable.isBlank()))
            .toList();

        if(invalidVariables.isEmpty()){
            return;
        }

        for (String variable : invalidVariables) {
            logger.error(variable + " can not be null or blank");
        }
        
        throw new IllegalArgumentException("Uri variables can not be null or blank");
    }
    
}
