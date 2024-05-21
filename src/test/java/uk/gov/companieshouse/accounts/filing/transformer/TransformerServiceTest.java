package uk.gov.companieshouse.accounts.filing.transformer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;

@ExtendWith(MockitoExtension.class)
class TransformerServiceTest {

    private static final String RESOURCE_KIND = "abc";

    private TransactionTransformer service;

    @BeforeEach
    void setUp() {
        service = new TransactionTransformerImpl();
        ReflectionTestUtils.setField(service, "resourceKind", "abc");
    }

    @Test
    void testSetupTransactionWithCICPackageType() {
        var transaction = new Transaction();
        var transactionId = "transactionId";
        var accountFilingId = "accountFilingId";
        var uri = "/transactions/" + transactionId + "/accounts-filing/" + accountFilingId;

        Map<String, String> links = Map.of(
                "resource", uri,
                "validation_status", uri + "/validation-status",
                "costs",uri + "/costs");

        transaction.setId(transactionId);
        transaction.setResources(new HashMap<>());

        // then
        service.setupTransactionResources(transaction, accountFilingId, "cic");
        Resource resourceResult = transaction.getResources().get(uri);

        assertEquals(1, transaction.getResources().size());
        assertTrue(transaction.getResources().containsKey(uri));
        assertEquals(RESOURCE_KIND, resourceResult.getKind());
        assertEquals(3, resourceResult.getLinks().size());
        assertEquals(links, resourceResult.getLinks());
        assertNotNull(resourceResult.getUpdatedAt());
    }

    @Test
    void testSetupTransactionWithOverseasPackageType() {
        var transaction = new Transaction();
        var transactionId = "transactionId";
        var accountFilingId = "accountFilingId";
        var uri = "/transactions/" + transactionId + "/accounts-filing/" + accountFilingId;

        Map<String, String> links = Map.of(
                "resource", uri,
                "validation_status", uri + "/validation-status",
                "costs",uri + "/costs");

        transaction.setId(transactionId);
        transaction.setResources(new HashMap<>());

        // then
        service.setupTransactionResources(transaction, accountFilingId, "overseas");
        Resource resourceResult = transaction.getResources().get(uri);

        assertEquals(1, transaction.getResources().size());
        assertTrue(transaction.getResources().containsKey(uri));
        assertEquals(RESOURCE_KIND, resourceResult.getKind());
        assertEquals(3, resourceResult.getLinks().size());
        assertEquals(links, resourceResult.getLinks());
        assertNotNull(resourceResult.getUpdatedAt());
    }

    @Test
    void testSetupTransactionWithOtherPackageType() {
        var transaction = new Transaction();
        var transactionId = "transactionId";
        var accountFilingId = "accountFilingId";
        var uri = "/transactions/" + transactionId + "/accounts-filing/" + accountFilingId;

        Map<String, String> links = Map.of(
                "resource", uri,
                "validation_status", uri + "/validation-status");

        transaction.setId(transactionId);
        transaction.setResources(new HashMap<>());

        // then
        service.setupTransactionResources(transaction, accountFilingId, "uksef");
        Resource resourceResult = transaction.getResources().get(uri);

        assertEquals(1, transaction.getResources().size());
        assertTrue(transaction.getResources().containsKey(uri));
        assertEquals(RESOURCE_KIND, resourceResult.getKind());
        assertEquals(2, resourceResult.getLinks().size());
        assertEquals(links, resourceResult.getLinks());
        assertNotNull(resourceResult.getUpdatedAt());
    }
}
