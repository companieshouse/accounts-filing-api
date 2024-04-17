package uk.gov.companieshouse.accounts.filing.service.company;

import uk.gov.companieshouse.accounts.filing.model.CompanyResponse;

public interface CompanyService {
    CompanyResponse saveCompanyNumberAndTransactionId(String companyNumber, String transactionId, String companyName);
}
