package uk.gov.companieshouse.accounts.filing.service.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.companieshouse.accounts.filing.exceptionhandler.ExternalServiceException;
import uk.gov.companieshouse.accounts.filing.exceptionhandler.ResponseException;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    TransactionAPI transactionAPI;

    @Spy
    Logger logger;

    TransactionService transactionService;

    @BeforeEach
    void beforeEach() {
        transactionService = new TransactionServiceImpl(transactionAPI, logger);
    }

    @Test
    @DisplayName("Request a transaction")
    void testGetTransaction() throws ApiErrorResponseException, URIValidationException {
        var transactionId = "transactionId";
        var transaction = new Transaction();
        ApiResponse<Transaction> apiResponse = new ApiResponse<>(200, Collections.emptyMap(), transaction);

        when(transactionAPI.get(transactionId)).thenReturn(apiResponse);

        Optional<Transaction> returnTransaction = transactionService.getTransaction(transactionId);

        assertTrue(returnTransaction.isPresent());
        assertEquals(returnTransaction.get(), transaction);
    }

    @Test
    @DisplayName("Request a transaction with non-matching id")
    void testGetTransactionFailedWithNonMatchingId() throws ApiErrorResponseException, URIValidationException {
        var transactionId = "transactionId";
        ApiErrorResponseException exception = mock(ApiErrorResponseException.class);
        when(exception.getStatusCode()).thenReturn(404);
        when(transactionAPI.get(transactionId)).thenThrow(exception);

        Optional<Transaction> returnTransaction = transactionService.getTransaction(transactionId);

        assertTrue(returnTransaction.isEmpty());
    }

    @Test
    @DisplayName("Request a transaction get 500 back")
    void testGetTransactionFailedCausedByExternalService() throws ApiErrorResponseException, URIValidationException {
        var transactionId = "transactionId";
        ApiErrorResponseException exception = mock(ApiErrorResponseException.class);
        when(exception.getStatusCode()).thenReturn(500);
        when(transactionAPI.get(transactionId)).thenThrow(exception);

        assertThrows(ExternalServiceException.class, () -> transactionService.getTransaction(transactionId));
    }

    @Test
    @DisplayName("Update a transaction")
    void testUpdateTransaction() throws ApiErrorResponseException, URIValidationException {
        var transaction = new Transaction();
        ApiResponse<Void> apiResponse = new ApiResponse<>(204, Collections.emptyMap());

        when(transactionAPI.patch(transaction)).thenReturn(apiResponse);

        transactionService.updateTransaction(transaction);

        verify(transactionAPI, times(1)).patch(transaction);
        verify(logger, never()).errorContext(anyString(), any(Exception.class), anyMap());
    }

    @Test
    @DisplayName("Update a transaction returns wrong http status and throws exception")
    void testUpdateTransactionAPIReturnWrongType() throws ApiErrorResponseException, URIValidationException {
        var transaction = new Transaction();
        ApiErrorResponseException exception = mock(ApiErrorResponseException.class);
        when(exception.getStatusCode()).thenReturn(401);
        when(transactionAPI.patch(transaction)).thenThrow(exception);

        assertThrows(ResponseException.class, () -> transactionService.updateTransaction(transaction));
        verify(transactionAPI, times(1)).patch(transaction);
    }

}
