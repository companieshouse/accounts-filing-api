package uk.gov.companieshouse.accounts.filing.service.costs;

import uk.gov.companieshouse.accounts.filing.model.AccountsFilingEntry;
import uk.gov.companieshouse.api.model.payment.CostsApi;

public interface CostsService {

    CostsApi calculateCosts(AccountsFilingEntry accountsFilingEntry);
}
