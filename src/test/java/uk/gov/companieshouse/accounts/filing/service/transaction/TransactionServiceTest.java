package uk.gov.companieshouse.accounts.filing.service.transaction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.companieshouse.accounts.filing.exceptionhandler.ResponseException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.api.model.transaction.Resource;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    private static final String RESOURCE_KIND = "accounts-filing-api";

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
    void testGetTransaction() {
        final var transactionId = "transactionId";
        final var transaction = new Transaction();
        final ApiResponse<Transaction> apiResponse = new ApiResponse<>(200, Collections.emptyMap(), transaction);

        when(transactionAPI.get(transactionId)).thenReturn(apiResponse);

        final Optional<Transaction> returnTransaction = transactionService.getTransaction(transactionId);

        assertTrue(returnTransaction.isPresent());
        assertEquals(returnTransaction.get(), transaction);        

    }

    @Test
    @DisplayName("Update a transaction")
    void testUpdateTransaction() {
        final var transaction = new Transaction();
        final ApiResponse<Void> apiResponse = new ApiResponse<>(204, Collections.emptyMap());
        
        when(transactionAPI.patch(transaction)).thenReturn(apiResponse);
        
        transactionService.updateTransaction(transaction);

        verify(transactionAPI, times(1)).patch(transaction);
        verify(logger, never()).errorContext(anyString(), any(Exception.class), anyMap());
    }

    @Test
    @DisplayName("Update a transaction returns wrong http status and throws exception")
    void testUpdateTransactionAPIReturnWrongType() {
        final var transaction = new Transaction();
        final ApiResponse<Void> apiResponse = new ApiResponse<>(500, Collections.emptyMap());
        
        when(transactionAPI.patch(transaction)).thenReturn(apiResponse);
        
        assertThrows(ResponseException.class, () -> transactionService.updateTransaction(transaction));
        verify(transactionAPI, times(1)).patch(transaction);
    }


    @Test
    void testUpdateTransactionWithPackagetype() {
        final var transaction = new Transaction();
        final var transactionId = "transactionId";
        final var accountFilingId = "accountFilingId";
        final var packageTypeString = "packageType";
        final var uri = "/transactions/" + transactionId + "/account-filing/" + accountFilingId;
        // date in the past.
        final var localDateTime = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
        final Resource resource = new Resource();
        resource.setKind(RESOURCE_KIND);
        resource.setUpdatedAt(localDateTime);
        resource.setLinks(Collections.emptyMap());
        transaction.setId(transactionId);
        transaction.setResources(new HashMap<>());
        
        final ApiResponse<Void> apiResponse = new ApiResponse<>(204, Collections.emptyMap());
        
        //when
        when(transactionAPI.patch(any(Transaction.class))).thenReturn(apiResponse);


        //then
        transactionService.updateTransactionWithPackagetype(transaction, accountFilingId, packageTypeString);
        final Resource resourceResult = transaction.getResources().get(uri);

        verify(transactionAPI, times(1)).patch(transaction);
        assertEquals(1, transaction.getResources().size());
        assertTrue(transaction.getResources().containsKey(uri));
        assertEquals(resource.getKind(), resourceResult.getKind());
        assertEquals(resource.getLinks(), resourceResult.getLinks());
        assertNotEquals(resourceResult.getUpdatedAt(), localDateTime);
    }

    @Test
    void testUpdateTransactionWithPackagetypeWithInvalidParams() {
        final Transaction transaction = new Transaction();
        final String accountFilingId = null;
        final String packageTypeString = " ";

        assertThrows(IllegalArgumentException.class, () -> transactionService.updateTransactionWithPackagetype(transaction, accountFilingId, packageTypeString));
        verify(logger, times(2)).error(contains("can not be null or blank"));
    }
}
