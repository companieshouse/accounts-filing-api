package uk.gov.companieshouse.accounts.filing.repository;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.accounts.filing.model.ConfirmCompanyRecord;

@Repository
public interface AccountsFilingRepository extends MongoRepository<ConfirmCompanyRecord, String> {
}
