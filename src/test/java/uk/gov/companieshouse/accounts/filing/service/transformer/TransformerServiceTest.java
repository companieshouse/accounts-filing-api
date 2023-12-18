package uk.gov.companieshouse.accounts.filing.service.transformer;

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

import uk.gov.companieshouse.accounts.filing.transformer.TransactionTransformer;
import uk.gov.companieshouse.accounts.filing.transformer.TransactionTransformerImpl;
import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;


@ExtendWith(MockitoExtension.class)
class TransformerServiceTest {

    private final String RESOURCE_KIND = "abc";

    private TransactionTransformer service;

    @BeforeEach
    void setUp(){
        service = new TransactionTransformerImpl();
        ReflectionTestUtils.setField(service, "resourceKind", "abc");
    }

    @Test
    void testSetupTransaction() {
        final var transaction = new Transaction();
        final var transactionId = "transactionId";
        final var accountFilingId = "accountFilingId";
        final var uri = "/transactions/" + transactionId + "/account-filing/" + accountFilingId;
        
        final Map<String, String> links = Map.of(
            "resource", uri,
            "validation_status", uri+"/validation-status"
            );
            
        transaction.setId(transactionId);
        transaction.setResources(new HashMap<>());

        //then
        service.setupTransactionResources(transaction, accountFilingId);
        final Resource resourceResult = transaction.getResources().get(uri);

        assertEquals(1, transaction.getResources().size());
        assertTrue(transaction.getResources().containsKey(uri));
        assertEquals(RESOURCE_KIND, resourceResult.getKind());
        assertEquals(2, resourceResult.getLinks().size());
        assertEquals(links, resourceResult.getLinks());
        assertNotNull(resourceResult.getUpdatedAt());
    }
}
