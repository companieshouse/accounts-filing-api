package uk.gov.companieshouse.accounts.filing.service.costs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.accounts.filing.model.AccountsFilingEntry;
import uk.gov.companieshouse.accounts.filing.model.types.PackageType;
import uk.gov.companieshouse.api.model.payment.Cost;
import uk.gov.companieshouse.api.model.payment.CostsApi;

import java.util.*;

@Component
public class CostsServiceImpl implements CostsService{
    @Value("${fee.amount.cic.account}")
    private double cicAccountFeeAmount;

    @Value("${fee.amount.overseas.account}")
    private double overseasAccountFeeAmount;

    private static final String DEFAULT_PAYMENT_METHOD = "credit-card";

    private static final String DEFAULT_PAYMENT_CLASS = "data-maintenance";

    private static final String DEFAULT_DESCRIPTION = "Payment for %s Package Accounts for %s";

    private static final String DEFAULT_DESCRIPTION_ID = "description-identifier";

    private static final String DEFAULT_KEY = "key";

    private static final String DEFAULT_VALUE = "value";

    private static final String DEFAULT_KIND = "payment-session#payment-session";

    private static final String DEFAULT_PRODUCT_TYPE = "package-accounts";


    /**
     * This method returns the cost items with a fee based on the accounts package type
     * @param accountsFilingEntry - Account to be filed
     * @return CostsApi - Cost to file the account
     */
    @Override
    public CostsApi calculateCosts(AccountsFilingEntry accountsFilingEntry) {
        Cost cost;
        CostsApi costs = new CostsApi();
        costs.setItems(new ArrayList<>());
        if(PackageType.CIC.equals(accountsFilingEntry.getPackageType())){
            cost = createCostWithDefaultValues(accountsFilingEntry);
            cost.setAmount(Double.toString(cicAccountFeeAmount));
            costs.getItems().add(cost);
        }
        else if(PackageType.OVERSEAS.equals(accountsFilingEntry.getPackageType())){
            cost = createCostWithDefaultValues(accountsFilingEntry);
            cost.setAmount(Double.toString(overseasAccountFeeAmount));
            costs.getItems().add(cost);
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