package uk.gov.companieshouse.accounts.filing.service.costs;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.companieshouse.accounts.filing.model.AccountsFilingEntry;
import uk.gov.companieshouse.api.model.felixvalidator.PackageTypeApi;
import uk.gov.companieshouse.api.model.payment.Cost;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class CostsServiceTest {

    CostsService costsService;

    AccountsFilingEntry accountsFilingEntry;

    String cicAccountsFee = "15";

    String overseasAccountsFee = "33";

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
        accountsFilingEntry = new AccountsFilingEntry("", "", "", null, "", "", "", "");
    }

    @Test
    @DisplayName("Test calculateCosts returns £15 fee for CIC account")
    void testCalculateCostsForCicAccount(){
        accountsFilingEntry.setPackageType(PackageTypeApi.CIC);
        ReflectionTestUtils.setField(costsService, "cicAccountsFee", cicAccountsFee);
        List<Cost> costs = costsService.calculateCosts(accountsFilingEntry);
        assertFeeAndDefaultValues(costs, cicAccountsFee);
    }

    @Test
    @DisplayName("Test calculateCosts returns £33 fee for Overseas account")
    void testCalculateCostsForOverseasAccount(){
        accountsFilingEntry.setPackageType(PackageTypeApi.OVERSEAS);
        ReflectionTestUtils.setField(costsService, "overseasAccountsFee", overseasAccountsFee);
        List<Cost> costs = costsService.calculateCosts(accountsFilingEntry);
        assertFeeAndDefaultValues(costs, overseasAccountsFee, CostsServiceImpl.OVERSEAS_PRODUCT_TYPE, CostsServiceImpl.OVERSEAS_RESOURCE_KIND);
    }

    @Test
    @DisplayName("Test calculateCosts returns empty cost for other accounts")
    void testCalculateCostsForOtherAccounts(){
        accountsFilingEntry.setPackageType(PackageTypeApi.WELSH);
        List<Cost> costs = costsService.calculateCosts(accountsFilingEntry);
        Assertions.assertTrue(costs.isEmpty());
    }

    void assertFeeAndDefaultValues(List<Cost> costs, String expectedFee, String expectedProductType, String expectedResourceKind) {
        Assertions.assertNotNull(costs);
        Assertions.assertFalse(costs.isEmpty());
        Cost cost = costs.getFirst();
        Assertions.assertEquals(expectedFee, cost.getAmount());
        Assertions.assertEquals(DEFAULT_PAYMENT_METHOD, cost.getAvailablePaymentMethods().getFirst());
        Assertions.assertEquals(DEFAULT_PAYMENT_CLASS, cost.getClassOfPayment().getFirst());
        Assertions.assertNotNull(cost.getDescription());
        Assertions.assertEquals(DEFAULT_DESCRIPTION_ID, cost.getDescriptionIdentifier());
        Assertions.assertEquals(DEFAULT_VALUE, cost.getDescriptionValues().get(DEFAULT_KEY));
        Assertions.assertEquals(DEFAULT_KIND, cost.getKind());
        Assertions.assertEquals(expectedProductType, cost.getProductType());
        Assertions.assertEquals(expectedResourceKind, cost.getResourceKind());
    }

    void assertFeeAndDefaultValues(List<Cost> costs, String expectedFee) {
        assertFeeAndDefaultValues(costs, expectedFee, DEFAULT_PRODUCT_TYPE, DEFAULT_KIND);
    }
}