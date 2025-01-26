package uk.gov.companieshouse.accounts.filing.service.costs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.accounts.filing.model.AccountsFilingEntry;
import uk.gov.companieshouse.api.model.felixvalidator.PackageTypeApi;
import uk.gov.companieshouse.api.model.payment.Cost;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CostsServiceImpl implements CostsService{
    @Value("${fee.cic.accounts}")
    private String cicAccountsFee;

    @Value("${fee.overseas.accounts}")
    private String overseasAccountsFee;

    private static final String DEFAULT_PAYMENT_METHOD = "credit-card";

    private static final String DEFAULT_PAYMENT_CLASS = "data-maintenance";

    private static final String DEFAULT_DESCRIPTION = "Payment for %s Package Accounts for %s";

    private static final String DEFAULT_DESCRIPTION_ID = "description-identifier";

    private static final String DEFAULT_KEY = "key";

    private static final String DEFAULT_VALUE = "value";

    private static final String DEFAULT_KIND = "payment-session#payment-session";

    private static final String DEFAULT_PRODUCT_TYPE = "package-accounts";

    protected static final String CIC_RESOURCE_KIND = "cic-package-accounts";

    protected static final String CIC_PRODUCT_TYPE = "cic-package-accounts";

    /**
     * This method returns the cost items with a fee based on the accounts package type
     * @param accountsFilingEntry - Account to be filed
     * @return CostsApi - Cost to file the account
     */
    @Override
    public List<Cost> calculateCosts(AccountsFilingEntry accountsFilingEntry) {
        List<Cost> costs = new ArrayList<>();
        if(PackageTypeApi.CIC.equals(accountsFilingEntry.getPackageType())){
            Cost cost = createCostWithDefaultValues(accountsFilingEntry);
            cost.setProductType(CIC_PRODUCT_TYPE);
            cost.setResourceKind(CIC_RESOURCE_KIND);
            cost.setAmount(cicAccountsFee);
            costs.add(cost);
        }
        else if(PackageTypeApi.OVERSEAS.equals(accountsFilingEntry.getPackageType())){
            Cost cost = createCostWithDefaultValues(accountsFilingEntry);
            cost.setAmount(overseasAccountsFee);
            costs.add(cost);
        }
        return costs;
    }

    /**
     * This method assigns the default values and returns the cost items.
     * @param accountsFilingEntry - Account to be filed
     * @return CostsApi - Cost to file the account
     */
    private Cost createCostWithDefaultValues(AccountsFilingEntry accountsFilingEntry){
        Cost cost = new Cost();
        cost.setAvailablePaymentMethods(new ArrayList<>(List.of(DEFAULT_PAYMENT_METHOD)));
        cost.setClassOfPayment(new ArrayList<>(List.of(DEFAULT_PAYMENT_CLASS)));
        cost.setDescription(String.format(DEFAULT_DESCRIPTION, accountsFilingEntry.getPackageType(), accountsFilingEntry.getCompanyName()));
        cost.setDescriptionIdentifier(DEFAULT_DESCRIPTION_ID);
        Map<String, String> descriptionValues = new HashMap<>();
        descriptionValues.put(DEFAULT_KEY, DEFAULT_VALUE);
        cost.setDescriptionValues(descriptionValues);
        cost.setKind(DEFAULT_KIND);
        cost.setProductType(DEFAULT_PRODUCT_TYPE);
        cost.setResourceKind(DEFAULT_KIND);
        return cost;
    }
}