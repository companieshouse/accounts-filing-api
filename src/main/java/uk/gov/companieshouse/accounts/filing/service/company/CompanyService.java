package uk.gov.companieshouse.accounts.filing.service.company;

import uk.gov.companieshouse.accounts.filing.model.CompanyResponse;
import uk.gov.companieshouse.accounts.filing.model.CompanyRequest;

public interface CompanyService {
    CompanyResponse saveCompanyNumberAndTransactionId(String companyNumber, String transactionId, CompanyRequest companyRequest);
}
