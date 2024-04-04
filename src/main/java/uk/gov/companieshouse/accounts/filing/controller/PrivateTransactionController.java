package uk.gov.companieshouse.accounts.filing.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import uk.gov.companieshouse.accounts.filing.exceptionhandler.EntryNotFoundException;
import uk.gov.companieshouse.accounts.filing.mapper.FilingGeneratorMapper;
import uk.gov.companieshouse.accounts.filing.model.AccountsFilingEntry;
import uk.gov.companieshouse.accounts.filing.service.accounts.AccountsFilingService;
import uk.gov.companieshouse.accounts.filing.service.transaction.TransactionService;
import uk.gov.companieshouse.api.model.filinggenerator.FilingApi;
import uk.gov.companieshouse.logging.Logger;

@Controller
@RequestMapping("/private/transactions/{transactionId}/accounts-filing/{accountsFilingId}")
public class PrivateTransactionController {

    private final AccountsFilingService accountsFilingService;

    private final FilingGeneratorMapper filingGeneratorMapper;

    private final TransactionService transactionService;

    private final Logger logger;

    @Autowired
    public PrivateTransactionController(AccountsFilingService accountsFilingService,
    FilingGeneratorMapper filingGeneratorMapper,
    TransactionService transactionService,
    Logger logger){
        this.accountsFilingService = accountsFilingService;
        this.filingGeneratorMapper = filingGeneratorMapper;
        this.transactionService = transactionService;
        this.logger = logger;
    }

    @GetMapping("/filings")
    public ResponseEntity<FilingApi> getFilingApiEntry(
            @PathVariable("transactionId") final String transactionId,
            @PathVariable("accountsFilingId") final String accountsFilingId) {
        
        AccountsFilingEntry accountsFilingEntry;

        try{
            accountsFilingEntry = accountsFilingService.getFilingEntry(accountsFilingId);
        } catch (EntryNotFoundException ex){
            logger.error(String.format("%s: could did not match a known accountFilingId", accountsFilingId));
            return ResponseEntity.notFound().build();
        }

        if(transactionService.getTransaction(transactionId).isEmpty()){
            logger.error(String.format("%s: could did not match a known transactionId", accountsFilingId));
            return ResponseEntity.notFound().build();
        }

        if(!doesFilingEntryContainTransactionId(accountsFilingEntry, transactionId)){
            logger.error(String.format("TransactionId: %s is not linked to accountFilingId: %s.", transactionId, accountsFilingId));
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(filingGeneratorMapper.mapToFilingApi(accountsFilingEntry));
    }

    private boolean doesFilingEntryContainTransactionId(AccountsFilingEntry accountsFilingEntry, String transactionId) {
        if(accountsFilingEntry.getTransactionId() == null){
            return false;
        }
        return accountsFilingEntry.getTransactionId().equals(transactionId);
    }
}
