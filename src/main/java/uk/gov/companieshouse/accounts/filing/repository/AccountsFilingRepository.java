package uk.gov.companieshouse.accounts.filing.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import uk.gov.companieshouse.accounts.filing.model.AccountsFilingRecord;

public interface AccountsFilingRepository extends MongoRepository<AccountsFilingRecord, String>{
    
}
