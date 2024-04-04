package uk.gov.companieshouse.accounts.filing.controller;

import java.util.ArrayList;
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
import uk.gov.companieshouse.accounts.filing.service.file.validation.AccountsValidationService;
import uk.gov.companieshouse.accounts.filing.service.transaction.TransactionService;
import uk.gov.companieshouse.accounts.filing.transformer.TransactionTransformer;
import uk.gov.companieshouse.api.model.accountvalidator.AccountsValidatorStatusApi;
import uk.gov.companieshouse.api.model.payment.CostsApi;
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

    @Autowired
    public TransactionController(final AccountsValidationService accountsValidationService,
                                 final AccountsFilingService accountsFilingService,
                                 final TransactionService transactionService,
                                 final TransactionTransformer accountsFilingTransformer,
                                 final Logger logger) {
        this.accountsValidationService = accountsValidationService;
        this.accountsFilingService = accountsFilingService;
        this.accountsFilingTransformer = accountsFilingTransformer;
        this.transactionService = transactionService;
        this.logger = logger;
    }
    
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

    @PutMapping
    public ResponseEntity<String> setPackageType(@PathVariable("transactionId") final String transactionId, 
                                                 @PathVariable("accountsFilingId") final String accountsFilingId,
                                                 @Valid @RequestBody final AccountsPackageType packageType)
                                                 throws UriValidationException, EntryNotFoundException {
        
        final AccountsFilingEntry entry = accountsFilingService.getFilingEntry(accountsFilingId);
        accountsFilingService.savePackageType(entry, packageType.type());
        final Optional<Transaction> optionalTransaction = transactionService.getTransaction(transactionId);

        if (optionalTransaction.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        accountsFilingTransformer.setupTransactionResources(optionalTransaction.get(), accountsFilingId);
        transactionService.updateTransaction(optionalTransaction.get());
        
        return ResponseEntity.noContent().build();
    }

    /**
     * This method is used to validate the accounts filing data
     * @param transactionId - Transaction id
     * @param accountsFilingId - Filing id of the accounts
     * @return contains the validation status of the account filing
     */
    @GetMapping("/validation-status")
    public ResponseEntity<?> validateAccountsFilingData(@PathVariable("transactionId") final String transactionId,
                                                        @PathVariable("accountsFilingId") final String accountsFilingId){
        try{
            AccountsFilingEntry accountsFilingEntry = accountsFilingService.getAccountsFilingEntryForIDAndTransaction(transactionId,accountsFilingId);
            return ResponseEntity.ok(accountsFilingService.validateAccountsFilingEntry(accountsFilingEntry));
        }
        catch(EntryNotFoundException entryNotFoundException){
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * This method is used to calculate the costs for the filing
     * @param transactionId - Transaction id
     * @param accountsFilingId - Filing id of the accounts
     * @return contains the cost of the account filing
     */
    @GetMapping("/costs")
    public ResponseEntity<?> calculateCosts(@PathVariable("transactionId") final String transactionId,
                                            @PathVariable("accountsFilingId") final String accountsFilingId){
        try{
            AccountsFilingEntry accountsFilingEntry = accountsFilingService.getAccountsFilingEntryForIDAndTransaction(transactionId,accountsFilingId);
            CostsApi costs = new CostsApi();
            costs.setItems(new ArrayList<>());
            return ResponseEntity.ok(costs);
        }
        catch(EntryNotFoundException entryNotFoundException){
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Handles all un-caught exceptions
     *
     * @param ex the exception
     * @return response
     */
    @ExceptionHandler
    ResponseEntity<String> exceptionHandler(Exception ex) {
        return ControllerExceptionHandler.handleExpection(ex, logger);
    }
}
