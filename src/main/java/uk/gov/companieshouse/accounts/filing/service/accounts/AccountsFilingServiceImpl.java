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
import uk.gov.companieshouse.accounts.filing.utils.mapping.ImmutableConverter;
import uk.gov.companieshouse.api.model.validationstatus.ValidationStatusResponse;
import uk.gov.companieshouse.logging.Logger;

@Service
public class AccountsFilingServiceImpl implements AccountsFilingService {


    private final AccountsFilingRepository accountsFilingRepository;

    private final AccountsFilingValidator accountsFilingValidator;

    private final Logger logger;

    @Autowired
    public AccountsFilingServiceImpl(AccountsFilingRepository accountsFilingRepository,
                                     AccountsFilingValidator accountsFilingValidator, Logger logger) {
        this.accountsFilingRepository = accountsFilingRepository;
        this.accountsFilingValidator = accountsFilingValidator;
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
            logger.errorContext(accountsFilingId, message, null, ImmutableConverter.toMutableMap(Map.of(
                "expected", "accountsFilingEntry Object",
                "status", "empty optional"
                )));
            throw new EntryNotFoundException(message);
        }
        return optionalEntry.get();
    }

    /**
     * This method used to validate the data in accounts filing entry
     * @param accountsFilingEntry - accounts filing entry which needs to be validated
     * @return ValidationStatusResponse - Contains the validation status of the accounts filing entry
     */
    @Override
    public ValidationStatusResponse validateAccountsFilingEntry(AccountsFilingEntry accountsFilingEntry){
        return accountsFilingValidator.validateAccountsFilingEntry(accountsFilingEntry);
    }

    /**
     * This method used to get the accounts filing entry for the given transaction id and accounts filing id
     * @param transactionId - ID of the transaction
     * @param accountsFilingId - Filing id of the accounts
     * @return AccountsFilingEntry - returns accounts filing entry  for the given transaction id and accounts filing id.
     */
    @Override
    public AccountsFilingEntry getAccountsFilingEntryForIDAndTransaction(String transactionId, String accountsFilingId) {
        AccountsFilingEntry accountsFilingEntry = getFilingEntry(accountsFilingId);
        if(transactionId != null && transactionId.equals(accountsFilingEntry.getTransactionId())){
            return accountsFilingEntry;
        }
        else{
            var message = String.format("Entry with accountFilingId: %s and transaction id: %s was not found",
                                                        accountsFilingId, transactionId);
            logger.error(message);
            throw new EntryNotFoundException(message);
        }
    }
}