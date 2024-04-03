package uk.gov.companieshouse.accounts.filing.service.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.accounts.filing.service.api.ApiClientService;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.handler.privatetransaction.PrivateTransactionResourceHandler;
import uk.gov.companieshouse.api.handler.privatetransaction.request.PrivateTransactionGet;
import uk.gov.companieshouse.api.handler.privatetransaction.request.PrivateTransactionPatch;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.transaction.Transaction;

@ExtendWith(MockitoExtension.class)
public class TransactionApiTest {

    private static final String TRANSACTION_ID = "00000-00000-00000";
    private static final String PRIVATE_TRANSACTION_URL = "/private/transactions/";

    @Mock
    ApiClientService apiClientService;

    @Mock
    PrivateTransactionGet privateTransactionGet;

    @Mock
    InternalApiClient internalApiClient;

    @Mock
    PrivateTransactionResourceHandler privateTransactionResourceHandler;

    @Mock
    PrivateTransactionPatch privateTransactionPatch;

    @Captor
    ArgumentCaptor<String> pathCaptor;

    @Captor
    ArgumentCaptor<Transaction> transactionCaptor;

    TransactionAPI transactionAPI;

    @BeforeEach
    void setup() {
        transactionAPI = new TransactionAPI(apiClientService);
    }

    @Test
    void testGetReturnsCorrectly() throws Exception{
        Transaction transaction = new Transaction();
        ApiResponse<Transaction> expectedResponse = new ApiResponse<>(200, new HashMap<>(), transaction);

        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.privateTransaction()).thenReturn(privateTransactionResourceHandler);
        when(privateTransactionResourceHandler.get(pathCaptor.capture())).thenReturn(privateTransactionGet);
        when(privateTransactionGet.execute()).thenReturn(expectedResponse);

        ApiResponse<Transaction> actualResponse = transactionAPI.get(TRANSACTION_ID);

        assertEquals(expectedResponse.getStatusCode(), actualResponse.getStatusCode());
        assertEquals(PRIVATE_TRANSACTION_URL + TRANSACTION_ID, pathCaptor.getValue());
    }

    @Test
    void testPatchReturnsCorrectly() throws Exception{
        Transaction expectedTransaction = new Transaction();
        expectedTransaction.setId(TRANSACTION_ID);
        ApiResponse<Void> expectedResponse = new ApiResponse<>(204, new HashMap<>());

        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.privateTransaction()).thenReturn(privateTransactionResourceHandler);
        when(privateTransactionResourceHandler.patch(pathCaptor.capture(), transactionCaptor.capture()))
                .thenReturn(privateTransactionPatch);
        when(privateTransactionPatch.execute()).thenReturn(expectedResponse);

        ApiResponse<Void> actualResponse = transactionAPI.patch(expectedTransaction);

        assertEquals(expectedResponse.getStatusCode(), actualResponse.getStatusCode());
        assertEquals(PRIVATE_TRANSACTION_URL + TRANSACTION_ID, pathCaptor.getValue());
        assertEquals(expectedTransaction, transactionCaptor.getValue());
    }
}
