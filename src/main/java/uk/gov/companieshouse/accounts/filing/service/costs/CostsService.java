package uk.gov.companieshouse.accounts.filing.service.costs;

import uk.gov.companieshouse.accounts.filing.model.AccountsFilingEntry;
import uk.gov.companieshouse.api.model.payment.Cost;

import java.util.List;

public interface CostsService {

    List<Cost> calculateCosts(AccountsFilingEntry accountsFilingEntry);
}
