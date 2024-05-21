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

import uk.gov.companieshouse.api.model.felixvalidator.PackageTypeApi;
import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;


@ExtendWith(MockitoExtension.class)
class TransformerServiceTest {

    private static final String RESOURCE_KIND = "abc";

    private TransactionTransformer service;

    private Transaction transaction;

    private static final String TRANSACTION_ID = "transactionId";
    private static final String  ACCOUNTS_FILING_ID = "accountFilingId";
    private static final String URI = "/transactions/" + TRANSACTION_ID + "/accounts-filing/" + ACCOUNTS_FILING_ID;

    Map<String, String> linksWithCost = Map.of(
            "resource", URI,
            "validation_status", URI + "/validation-status",
            "costs",URI + "/costs");

    Map<String, String> linksWithNoCost = Map.of(
            "resource", URI,
            "validation_status", URI + "/validation-status");

    @BeforeEach
    void setUp() {
        transaction = new Transaction();
        transaction.setId(TRANSACTION_ID);
        transaction.setResources(new HashMap<>());
        service = new TransactionTransformerImpl();
        ReflectionTestUtils.setField(service, "resourceKind", "abc");
    }

    @Test
    void testSetupTransactionWithAllPackageTypes() {
        var packageTypes = PackageTypeApi.values();
        for(PackageTypeApi packageTypeApi : packageTypes){
            var packageType = packageTypeApi.toString();
            service.setupTransactionResources(transaction, ACCOUNTS_FILING_ID, packageType);
            Resource resourceResult = transaction.getResources().get(URI);
            assertEquals(1, transaction.getResources().size());
            assertTrue(transaction.getResources().containsKey(URI));
            assertEquals(RESOURCE_KIND, resourceResult.getKind());
            assertNotNull(resourceResult.getUpdatedAt());
            if(packageType == PackageTypeApi.CIC.toString() || packageType == PackageTypeApi.OVERSEAS.toString()){
                assertEquals(linksWithCost.size(), resourceResult.getLinks().size(), "Links with cost count is mismatch, please verify the logic for package type " + packageType);
                assertEquals(linksWithCost, resourceResult.getLinks(), "Links with cost is mismatch, please verify the logic for package type " + packageType);
            }
            else{
                assertEquals(linksWithNoCost.size(), resourceResult.getLinks().size(), "Links count mismatch, please verify the logic for package type " + packageType);
                assertEquals(linksWithNoCost, resourceResult.getLinks(), "Links mismatch, please verify the logic for package type" + packageType);
            }
        }
    }
}
