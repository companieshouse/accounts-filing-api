package uk.gov.companieshouse.accounts.filing.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;


import uk.gov.companieshouse.accounts.filing.exceptionhandler.ResponseException;
import uk.gov.companieshouse.accounts.filing.model.AccountsFilingEntry;
import uk.gov.companieshouse.accounts.filing.model.CompanyResponse;
import uk.gov.companieshouse.accounts.filing.repository.AccountsFilingRepository;
import uk.gov.companieshouse.logging.Logger;

import java.util.regex.Pattern;

@Controller
@RequestMapping("/accounts-filing")
public class AccountsFilingController {
    private Logger logger;
    private AccountsFilingRepository filingRepository;

    @Autowired
    public AccountsFilingController(Logger logger, AccountsFilingRepository filingRepository) {
        this.logger = logger;
        this.filingRepository = filingRepository;
    }

    @GetMapping("/healthcheck")
    public ResponseEntity<String> checking(){
        return ResponseEntity.ok().body("OK");
    }

    @PutMapping("/company/{companyNumber}/confirm/{transactionId}")
    public ResponseEntity<?> confirmCompany(@PathVariable final String companyNumber, @PathVariable final String transactionId){
        logger.info(String.format("Saving company number %s",
                companyNumber));
        if (!isRequestValid(companyNumber) || !isRequestValid(transactionId)) {
            return ResponseEntity.badRequest().build();
        }
        try {
            AccountsFilingEntry entry = new AccountsFilingEntry(null, null, null,null,companyNumber,transactionId);
           return ResponseEntity.ok(new CompanyResponse(filingRepository.save(entry).getAccountFilingId()));
        } catch(Exception ex) {
            return ResponseEntity.notFound().build();
        }
    }

    private boolean isRequestValid(String request) {
        if (request == null || request.isBlank()) {
            return false;
        } else {
            Pattern pattern = Pattern.compile("^[a-zA-Z0-9]+$");
            return pattern.matcher(request).matches();
        }
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
