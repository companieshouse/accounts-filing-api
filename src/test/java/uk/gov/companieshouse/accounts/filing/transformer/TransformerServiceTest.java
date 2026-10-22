package uk.gov.companieshouse.accounts.filing.transformer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import uk.gov.companieshouse.accounts.filing.model.AccountsFilingEntry;
import uk.gov.companieshouse.accounts.filing.model.types.AccountsType;
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
        ReflectionTestUtils.setField(service, "isRevisedCicEnabled", true);
    }

    @Test
    void testSetupTransactionWithAllPackageTypes() {
        var packageTypes = PackageTypeApi.values();
        for(PackageTypeApi packageTypeApi : packageTypes){
            var packageType = packageTypeApi.toString();
            service.setupTransactionResources(transaction, ACCOUNTS_FILING_ID, packageType, null);
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

    @ParameterizedTest
    @ValueSource(strings = {"51", "52", "53", "54", "55", "58", "59", "66", "67", "68"})
    void testAmendedAccountsByPassCostsLinkForCicAndOverseas(String amendedStemCode) {
        var entry = accountsFilingEntry(amendedStemCode);
        for (PackageTypeApi packageTypeApi : List.of(PackageTypeApi.CIC, PackageTypeApi.OVERSEAS)) {
            transaction.setResources(new HashMap<>());
            service.setupTransactionResources(transaction, ACCOUNTS_FILING_ID, packageTypeApi.toString(), entry);
            assertEquals(linksWithNoCost, transaction.getResources().get(URI).getLinks(),
                    "Amended accounts " + amendedStemCode + " should not have costs link for " + packageTypeApi);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"51", "68"})
    void testAmendedAccountsByNameByPassCostsLink(String amendedStemCode) {
        var entry = accountsFilingEntry(AccountsType.fromStemCode(amendedStemCode).getType());
        service.setupTransactionResources(transaction, ACCOUNTS_FILING_ID, PackageTypeApi.CIC.toString(), entry);
        assertEquals(linksWithNoCost, transaction.getResources().get(URI).getLinks());
    }

    @ParameterizedTest
    @ValueSource(strings = {"01", "04", "18", "00"})
    void testNonAmendedAccountsKeepCostsLinkForCicAndOverseas(String stemCode) {
        var entry = accountsFilingEntry(stemCode);
        for (PackageTypeApi packageTypeApi : List.of(PackageTypeApi.CIC, PackageTypeApi.OVERSEAS)) {
            transaction.setResources(new HashMap<>());
            service.setupTransactionResources(transaction, ACCOUNTS_FILING_ID, packageTypeApi.toString(), entry);
            assertEquals(linksWithCost, transaction.getResources().get(URI).getLinks(),
                    "Non-amended accounts " + stemCode + " should have costs link for " + packageTypeApi);
        }
    }

    private AccountsFilingEntry accountsFilingEntry(String accountsType) {
        var entry = new AccountsFilingEntry(ACCOUNTS_FILING_ID, "", "", null, TRANSACTION_ID, "", "", "");
        entry.setAccountsType(accountsType);
        return entry;
    }
}
