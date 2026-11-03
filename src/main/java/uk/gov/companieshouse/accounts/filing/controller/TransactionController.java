package uk.gov.companieshouse.accounts.filing.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.validation.Valid;
import uk.gov.companieshouse.accounts.filing.controller.handler.controller.exception.ControllerExceptionHandler;
import uk.gov.companieshouse.accounts.filing.exceptionhandler.EntryNotFoundException;
import uk.gov.companieshouse.accounts.filing.exceptionhandler.UriValidationException;
import uk.gov.companieshouse.accounts.filing.model.AccountsFilingEntry;
import uk.gov.companieshouse.accounts.filing.model.AccountsPackageType;
import uk.gov.companieshouse.accounts.filing.service.accounts.AccountsFilingService;
import uk.gov.companieshouse.accounts.filing.service.costs.CostsService;
import uk.gov.companieshouse.accounts.filing.service.file.validation.AccountsValidationService;
import uk.gov.companieshouse.accounts.filing.service.transaction.TransactionService;
import uk.gov.companieshouse.accounts.filing.transformer.TransactionTransformer;
import uk.gov.companieshouse.api.model.accountvalidator.AccountsValidatorStatusApi;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;

@Controller
@RequestMapping("/transactions/{transactionId}/accounts-filing/{accountsFilingId}")
public class TransactionController {

    private final Logger logger;
    private final AccountsValidationService accountsValidationService;
    private final AccountsFilingService accountsFilingService;
    private final TransactionService transactionService;
    private final TransactionTransformer accountsFilingTransformer;
    private final CostsService costsService;

    @Autowired
    public TransactionController(final Logger logger, final AccountsValidationService accountsValidationService,
                                 final AccountsFilingService accountsFilingService, final TransactionService transactionService,
                                 final TransactionTransformer accountsFilingTransformer, final CostsService costsService) {
        this.logger = logger;
        this.accountsValidationService = accountsValidationService;
        this.accountsFilingService = accountsFilingService;
        this.transactionService = transactionService;
        this.accountsFilingTransformer = accountsFilingTransformer;
        this.costsService = costsService;
    }

    /**
     * Retrieves the validator status for a file and saves it to the accounts filing entry when available.
     *
     * @param fileId the identifier of the file
     * @param accountsFilingId the identifier of the accounts filing
     * @return the validator status, or 404 when no status is available
     */
    @GetMapping("/file/{fileId}/status")
    public ResponseEntity<AccountsValidatorStatusApi> fileAccountsValidatorStatus(@PathVariable("fileId") final String fileId, @PathVariable("accountsFilingId") final String accountsFilingId){
        
        final Optional<AccountsValidatorStatusApi> accountsValidationResultOptional = accountsValidationService.validationStatusResult(fileId);

        if (accountsValidationResultOptional.isPresent()) {
            final AccountsFilingEntry filingEntry = accountsValidationService.getFilingEntry(accountsFilingId);
            accountsValidationService.saveFileValidationResult(filingEntry, accountsValidationResultOptional.get());
        }

        //.of() returns a 200 when optional resolves to an object and 404 when optional is empty
        return ResponseEntity.of(accountsValidationResultOptional);
    }

    /**
     * Saves the package type for an accounts filing and updates its transaction resources.
     *
     * @param transactionId the identifier of the transaction
     * @param accountsFilingId the identifier of the accounts filing
     * @param packageType the package type to save
     * @return 204 when updated, or 404 when the transaction is not found
     * @throws UriValidationException if a URI is invalid
     * @throws EntryNotFoundException if the accounts filing entry is not found
     */
    @PutMapping
    public ResponseEntity<String> setPackageType(@PathVariable("transactionId") final String transactionId, 
                                                 @PathVariable("accountsFilingId") final String accountsFilingId,
                                                 @Valid @RequestBody final AccountsPackageType packageType)
                                                 throws UriValidationException, EntryNotFoundException {
        
        final AccountsFilingEntry entry = accountsFilingService.getFilingEntry(accountsFilingId);
        accountsFilingService.savePackageType(entry, packageType.type());
        if (updateTransactionResources(transactionId, accountsFilingId, packageType.type(), entry).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.noContent().build();
    }

    /**
     * Validates the accounts filing entry after updating its transaction resources.
     *
     * @param transactionId the identifier of the transaction
     * @param accountsFilingId the identifier of the accounts filing
     * @return the validation result, or 404 if the filing entry or transaction is not found
     */
    @GetMapping("/validation-status")
    public ResponseEntity<?> validateAccountsFilingData(@PathVariable("transactionId") final String transactionId,
                                                        @PathVariable("accountsFilingId") final String accountsFilingId){
        try{
            final AccountsFilingEntry accountsFilingEntry = accountsFilingService.getAccountsFilingEntryForIDAndTransaction(transactionId,accountsFilingId);

            if (updateTransactionResources(transactionId, accountsFilingId,
                    accountsFilingEntry.getPackageType().toString(), accountsFilingEntry).isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(accountsFilingService.validateAccountsFilingEntry(accountsFilingEntry));
        }
        catch(final EntryNotFoundException entryNotFoundException){
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Calculates the costs for an accounts filing.
     *
     * @param transactionId the identifier of the transaction
     * @param accountsFilingId the identifier of the accounts filing
     * @return the calculated costs, or 404 if the filing entry is not found
     */
    @GetMapping("/costs")
    public ResponseEntity<?> calculateCosts(@PathVariable("transactionId") final String transactionId,
                                            @PathVariable("accountsFilingId") final String accountsFilingId){
        try{
            final AccountsFilingEntry accountsFilingEntry = accountsFilingService.getAccountsFilingEntryForIDAndTransaction(transactionId,accountsFilingId);
            return ResponseEntity.ok(costsService.calculateCosts(accountsFilingEntry));
        }
        catch(final EntryNotFoundException entryNotFoundException){
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Updates the transaction resources for an accounts filing when the transaction exists.
     *
     * @param transactionId the identifier of the transaction
     * @param accountsFilingId the identifier of the accounts filing
     * @param packageType the package type to apply
     * @param filingEntry the accounts filing entry used to configure the resources
     * @return the transaction when found, or an empty optional otherwise
     */
    private Optional<Transaction> updateTransactionResources(final String transactionId,
                                                             final String accountsFilingId,
                                                             final String packageType,
                                                             final AccountsFilingEntry filingEntry) {
        final Optional<Transaction> optionalTransaction = transactionService.getTransaction(transactionId);
        optionalTransaction.ifPresent(transaction -> {
            accountsFilingTransformer.setupTransactionResources(transaction, accountsFilingId, packageType, filingEntry);
            transactionService.updateTransaction(transaction);
        });
        return optionalTransaction;
    }

    /**
     * Converts uncaught exceptions into the controller's standard error response.
     *
     * @param ex the exception to handle
     * @return the error response
     */
    @ExceptionHandler
    ResponseEntity<String> exceptionHandler(final Exception ex) {
        return ControllerExceptionHandler.handleExpection(ex, logger);
    }
}