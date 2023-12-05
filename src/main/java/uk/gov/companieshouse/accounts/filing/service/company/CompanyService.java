package uk.gov.companieshouse.accounts.filing.service.company;

import uk.gov.companieshouse.accounts.filing.model.CompanyResponse;

public interface CompanyService {
    public CompanyResponse saveCompanyNumberAndTransactionId(String companyNumber, String transactionId);
}
