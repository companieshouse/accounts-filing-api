package uk.gov.companieshouse.accounts.filing.service.accounts;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.gov.companieshouse.accounts.filing.exceptionhandler.EntryNotFoundException;
import uk.gov.companieshouse.accounts.filing.exceptionhandler.UriValidationException;
import uk.gov.companieshouse.accounts.filing.model.AccountsFilingEntry;
import uk.gov.companieshouse.accounts.filing.model.enums.PackageType;
import uk.gov.companieshouse.accounts.filing.repository.AccountsFilingRepository;
import uk.gov.companieshouse.logging.Logger;

@Service
public class AccountsFilingServiceImpl implements AccountsFilingService {


    private AccountsFilingRepository accountsFilingRepository;
    private Logger logger;

    @Autowired
    public AccountsFilingServiceImpl(AccountsFilingRepository accountsFilingRepository, Logger logger){
        this.accountsFilingRepository = accountsFilingRepository;
        this.logger = logger;
    }

    @Override
    public void savePackageType(AccountsFilingEntry accountsFilingEntry, String packageType) throws UriValidationException {

        accountsFilingEntry.setPackageType(PackageType.findPackageType(packageType));
        
        accountsFilingRepository.save(accountsFilingEntry);
        var message = String.format("Account filing id: %s has been updated to include package type: %s",
        accountsFilingEntry.getAccountsFilingId(), packageType);
        logger.debug(message);
    }

    @Override
    public AccountsFilingEntry getFilingEntry(String accountsFilingId) throws EntryNotFoundException {
        Optional<AccountsFilingEntry> optionalEntry = accountsFilingRepository.findById(accountsFilingId);
        
        if (optionalEntry.isEmpty()) {
            var message = String.format("Entry with accountFilingId: %s was not found", accountsFilingId);
            logger.errorContext(accountsFilingId, message, null, Map.of(
                "expected", "accountsFilingEntry Object",
                "status", "empty optional"
                ));
            throw new EntryNotFoundException(message);
        }
        return optionalEntry.get();
    }
    
}
