package uk.gov.companieshouse.accounts.filing.service.costs;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.companieshouse.accounts.filing.model.AccountsFilingEntry;
import uk.gov.companieshouse.accounts.filing.model.types.PackageType;
import uk.gov.companieshouse.api.model.payment.Cost;
import uk.gov.companieshouse.api.model.payment.CostsApi;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class CostsServiceTest {

    CostsService costsService;

    AccountsFilingEntry accountsFilingEntry;

    @Value("${fee.amount.cic.account}")
    double cicAccountFeeAmount;

    @Value("${fee.amount.overseas.account}")
    double overseasAccountFeeAmount;

    String DEFAULT_PAYMENT_METHOD = "credit-card";

    String DEFAULT_PAYMENT_CLASS = "data-maintenance";

    String DEFAULT_DESCRIPTION_ID = "description-identifier";

    String DEFAULT_KEY = "key";

    String DEFAULT_VALUE = "value";

    String DEFAULT_KIND = "payment-session#payment-session";

    String DEFAULT_PRODUCT_TYPE = "package-accounts";

    @BeforeEach
    void setUp(){
        costsService = new CostsServiceImpl();
        accountsFilingEntry = new AccountsFilingEntry("", "", "", null, "", "", "");
    }

    @Test
    @DisplayName("Test calculateCosts returns £15 fee for CIC account")
    void testCalculateCostsForCicAccount(){
        accountsFilingEntry.setPackageType(PackageType.CIC);
        CostsApi costs = costsService.calculateCosts(accountsFilingEntry);
        assertFeeAndDefaultValues(costs, Double.toString(cicAccountFeeAmount));
    }

    @Test
    @DisplayName("Test calculateCosts returns £33 fee for Overseas account")
    void testCalculateCostsForOverseasAccount(){
        accountsFilingEntry.setPackageType(PackageType.OVERSEAS);
        CostsApi costs = costsService.calculateCosts(accountsFilingEntry);
        assertFeeAndDefaultValues(costs, Double.toString(overseasAccountFeeAmount));
    }

    @Test
    @DisplayName("Test calculateCosts returns empty cost for other accounts")
    void testCalculateCostsForOtherAccounts(){
        accountsFilingEntry.setPackageType(PackageType.UKSEF);
        CostsApi costs = costsService.calculateCosts(accountsFilingEntry);
        Assertions.assertTrue(costs.getItems().isEmpty());
    }

    void assertFeeAndDefaultValues(CostsApi costs, String expectedFee){
        List<Cost> costItems = costs.getItems();
        Assertions.assertNotNull(costItems);
        Assertions.assertFalse(costItems.isEmpty());
        Cost cost = costItems.getFirst();
        Assertions.assertEquals(expectedFee, cost.getAmount());
        Assertions.assertEquals(DEFAULT_PAYMENT_METHOD, cost.getAvailablePaymentMethods().getFirst());
        Assertions.assertEquals(DEFAULT_PAYMENT_CLASS, cost.getClassOfPayment().getFirst());
        Assertions.assertNotNull(cost.getDescription());
        Assertions.assertEquals(DEFAULT_DESCRIPTION_ID, cost.getDescriptionIdentifier());
        Assertions.assertEquals(DEFAULT_VALUE, cost.getDescriptionValues().get(DEFAULT_KEY));
        Assertions.assertEquals(DEFAULT_KIND, cost.getKind());
        Assertions.assertEquals(DEFAULT_PRODUCT_TYPE, cost.getProductType());
        Assertions.assertEquals(DEFAULT_KIND, cost.getResourceKind());
    }
}