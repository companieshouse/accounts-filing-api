package uk.gov.companieshouse.accounts.filing.transformer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import uk.gov.companieshouse.api.model.felixvalidator.PackageTypeApi;
import uk.gov.companieshouse.accounts.filing.model.types.AccountsType;
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
            service.setupTransactionResources(transaction, ACCOUNTS_FILING_ID, packageType, "01");
            Resource resourceResult = transaction.getResources().get(URI);
            assertEquals(1, transaction.getResources().size());
            assertTrue(transaction.getResources().containsKey(URI));
            assertEquals(RESOURCE_KIND, resourceResult.getKind());
            assertNotNull(resourceResult.getUpdatedAt());
            if(Objects.equals(packageType, PackageTypeApi.CIC.toString()) || Objects.equals(packageType, PackageTypeApi.OVERSEAS.toString())){
                assertEquals(linksWithCost.size(), resourceResult.getLinks().size(), "Links with cost count is mismatch, please verify the logic for package type " + packageType);
                assertEquals(linksWithCost, resourceResult.getLinks(), "Links with cost is mismatch, please verify the logic for package type " + packageType);
            }
            else{
                assertEquals(linksWithNoCost.size(), resourceResult.getLinks().size(), "Links count mismatch, please verify the logic for package type " + packageType);
                assertEquals(linksWithNoCost, resourceResult.getLinks(), "Links mismatch, please verify the logic for package type" + packageType);
            }
        }

    }

    @ParameterizedTest
    @EnumSource(value = AccountsType.class, names = ".*_AMENDED", mode = EnumSource.Mode.MATCH_ALL)
    void testAmendedAccountsHaveNoCostsLink(AccountsType accountsType) {
        for (PackageTypeApi packageType : new PackageTypeApi[]{PackageTypeApi.CIC, PackageTypeApi.OVERSEAS}) {
            for (String value : new String[]{accountsType.getStemCode(), accountsType.getType()}) {
                service.setupTransactionResources(transaction, ACCOUNTS_FILING_ID, packageType.toString(), value);

                assertEquals(linksWithNoCost, transaction.getResources().get(URI).getLinks(),
                        "Unexpected costs link for " + packageType + " with accounts type " + value);
            }
        }
    }

    @ParameterizedTest
    @EnumSource(value = AccountsType.class, names = ".*_AMENDED", mode = EnumSource.Mode.MATCH_NONE)
    void testOriginalAccountsRetainCostsLink(AccountsType accountsType) {
        for (PackageTypeApi packageType : new PackageTypeApi[]{PackageTypeApi.CIC, PackageTypeApi.OVERSEAS}) {
            for (String value : new String[]{accountsType.getStemCode(), accountsType.getType()}) {
                service.setupTransactionResources(transaction, ACCOUNTS_FILING_ID, packageType.toString(), value);

                assertEquals(linksWithCost, transaction.getResources().get(URI).getLinks());
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "unrecognised", "050"})
    void testUnrecognisedAccountsTypeRetainsCostsLink(String accountsType) {
        service.setupTransactionResources(transaction, ACCOUNTS_FILING_ID, PackageTypeApi.CIC.toString(),
                accountsType);

        assertEquals(linksWithCost, transaction.getResources().get(URI).getLinks());
    }

    @Test
    void testInitialisesMissingResources() {
        transaction.setResources(null);

        service.setupTransactionResources(transaction, ACCOUNTS_FILING_ID, PackageTypeApi.CIC.toString(), null);

        assertEquals(1, transaction.getResources().size());
        Resource resource = transaction.getResources().get(URI);
        assertEquals(linksWithNoCost, resource.getLinks());
        assertEquals(RESOURCE_KIND, resource.getKind());
        assertNotNull(resource.getUpdatedAt());
    }

    @Test
    void testReplacesFilingResourceAndPreservesOtherResources() {
        Resource existingResource = new Resource();
        Resource otherResource = new Resource();
        String otherUri = "/transactions/" + TRANSACTION_ID + "/accounts-filing/other";
        transaction.getResources().put(URI, existingResource);
        transaction.getResources().put(otherUri, otherResource);
        Map<String, Resource> resources = transaction.getResources();

        service.setupTransactionResources(transaction, ACCOUNTS_FILING_ID, PackageTypeApi.CIC.toString(), "51");

        assertSame(resources, transaction.getResources());
        assertEquals(2, resources.size());
        assertSame(otherResource, resources.get(otherUri));
        Resource resource = resources.get(URI);
        assertEquals(linksWithNoCost, resource.getLinks());
        assertEquals(RESOURCE_KIND, resource.getKind());
        assertNotNull(resource.getUpdatedAt());
    }
}
