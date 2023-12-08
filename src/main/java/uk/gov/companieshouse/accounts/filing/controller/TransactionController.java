package uk.gov.companieshouse.accounts.filing.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import uk.gov.companieshouse.accounts.filing.exceptionhandler.EntryNotFoundException;
import uk.gov.companieshouse.accounts.filing.exceptionhandler.ResponseException;
import uk.gov.companieshouse.accounts.filing.exceptionhandler.UriValidationException;
import uk.gov.companieshouse.accounts.filing.model.AccountsFilingEntry;
import uk.gov.companieshouse.accounts.filing.service.file.validation.AccountsValidationService;
import uk.gov.companieshouse.api.model.accountvalidator.AccountsValidatorStatusApi;
import uk.gov.companieshouse.logging.Logger;

@Controller
@RequestMapping("/transactions/{transactionId}/accounts-filing/{accountsFilingId}")
public class TransactionController {

    private final Logger logger;
    private final AccountsValidationService accountsValidationService;

    @Autowired
    public TransactionController(AccountsValidationService accountsValidationService, Logger logger) {
        this.accountsValidationService = accountsValidationService;
        this.logger = logger;
    }
    
    @GetMapping("/file/{fileId}/status")
    public ResponseEntity<AccountsValidatorStatusApi> fileAccountsValidatorStatus(@PathVariable("fileId") final String fileId, @PathVariable("accountsFilingId") final String accountsFilingId){
        Optional<AccountsValidatorStatusApi> accountsValidationResultOptional = accountsValidationService.validationStatusResult(fileId);

        if (accountsValidationResultOptional.isPresent()) {
            AccountsFilingEntry filingEntry = accountsValidationService.getFilingEntry(accountsFilingId);
            accountsValidationService.saveFileValidationResult(filingEntry, accountsValidationResultOptional.get());
        }

        //.of() returns a 200 when optional resolves to an object and 404 when optional is empty
        return ResponseEntity.of(accountsValidationResultOptional);
    }

    /**
     * Handles the exception thrown when there's a response problem
     *
     * @return 400 bad request response
     */
    @ExceptionHandler({ResponseException.class})
    ResponseEntity<String> responseException(ResponseException e) {
        logger.error("Unhandled response exception", e);
        return ResponseEntity.badRequest().body("Api Response failed. " + e.getMessage());
    }

    /**
     * Handles the exception thrown when there's a validation problem
     *
     * @return 400 bad request response
     */
    @ExceptionHandler({UriValidationException.class})
    ResponseEntity<String> validationException() {
        return ResponseEntity.badRequest().body("Validation failed");
    }

    @ExceptionHandler({EntryNotFoundException.class})
    ResponseEntity<String> entryNotFoundException() {
        return ResponseEntity.notFound().build();
    }

    /**
     * Handles all un-caught exceptions
     *
     * @param ex the exception
     * @return 500 internal server error response
     */
    @ExceptionHandler
    ResponseEntity<String> exceptionHandler(Exception ex) {
        logger.error("Unhandled exception", ex);

        return ResponseEntity.internalServerError().build();
    }
}
