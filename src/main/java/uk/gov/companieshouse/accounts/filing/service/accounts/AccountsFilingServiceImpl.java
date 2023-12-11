package uk.gov.companieshouse.accounts.filing.service.accounts;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.gov.companieshouse.accounts.filing.exceptionhandler.EntryNotFoundException;
import uk.gov.companieshouse.accounts.filing.exceptionhandler.UriValidationException;
import uk.gov.companieshouse.accounts.filing.model.AccountsFilingEntry;
import uk.gov.companieshouse.accounts.filing.model.enums.PackageType;
import uk.gov.companieshouse.accounts.filing.repository.AccountsFilingRepository;
import uk.gov.companieshouse.accounts.filing.service.transaction.TransactionService;
import uk.gov.companieshouse.accounts.filing.utils.mapping.ImmutableConverter;
import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;

@Service
public class AccountsFilingServiceImpl implements AccountsFilingService {

    private static final String RESOURCE_KIND = "accounts-filing-api";

    private AccountsFilingRepository accountsFilingRepository;
    private TransactionService transactionService;
    private Logger logger;

    @Autowired
    public AccountsFilingServiceImpl(AccountsFilingRepository accountsFilingRepository, TransactionService transactionService, Logger logger){
        this.accountsFilingRepository = accountsFilingRepository;
        this.transactionService = transactionService;
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

    @Override
    public void updateAccountsFilingTransaction(final Transaction transaction, final String accountsFilingId) {

        checkUriVariablesAreNullOrBlank(transaction.getId(), accountsFilingId);
        
        final var uri = String.format("/transactions/%s/account-filing/%s", transaction.getId(), accountsFilingId);
        
        Map<String, String> links = ImmutableConverter.toMutableMap(
            Map.of(
                "costs", uri+"/costs",
                "resource", uri,
                "validation_status", uri+"/validation-status"
            )
        );
        
        final Resource resource = new Resource();
        resource.setKind(RESOURCE_KIND);
        resource.setUpdatedAt(LocalDateTime.now());
        resource.setLinks(links);
        transaction.getResources().put(uri, resource);
        transactionService.updateTransaction(transaction);
    }

    private void checkUriVariablesAreNullOrBlank(String ...variables){
        var invalidVariables = Stream.of(variables)
            .filter(variable -> (variable == null || variable.isBlank()))
            .toList();

        if(invalidVariables.isEmpty()){
            return;
        }

        for (String variable : invalidVariables) {
            logger.error(variable + " can not be null or blank");
        }
        
        throw new IllegalArgumentException("Uri variables can not be null or blank");
    }
    
}
