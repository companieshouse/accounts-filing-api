package uk.gov.companieshouse.accounts.filing.service.company;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.accounts.filing.model.AccountsFilingEntry;
import uk.gov.companieshouse.accounts.filing.model.CompanyResponse;
import uk.gov.companieshouse.accounts.filing.repository.AccountsFilingRepository;
import uk.gov.companieshouse.logging.Logger;

import java.util.Map;

@Service
public class CompanyServiceImpl implements CompanyService{

    private final Logger logger;
    private final AccountsFilingRepository accountsFilingRepository;
    @Autowired
    public CompanyServiceImpl(
            Logger logger,
            AccountsFilingRepository accountsFilingRepository) {
        this.logger = logger;
        this.accountsFilingRepository = accountsFilingRepository;
    }

    @Override
    public CompanyResponse saveCompanyNumberAndTransactionId(String companyNumber, String transactionId) throws RuntimeException {
        var response = new CompanyResponse(accountsFilingRepository.save(
                new AccountsFilingEntry(null, null, null,null,
                        companyNumber, transactionId)).getAccountFilingId());
        if(response.accountsFilingId().isBlank()){
            var message = "Unexpected error from mongodb when trying to save company number and transaction id";
            logger.errorContext(companyNumber, message, null, Map.of(
                    "expected", "accountsFilingId",
                    "actual", response.accountsFilingId()
            ));
            throw new RuntimeException(message);
        }
        return response;
    }
}
