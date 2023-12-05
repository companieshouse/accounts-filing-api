package uk.gov.companieshouse.accounts.filing.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.companieshouse.accounts.filing.exceptionhandler.ResponseException;
import uk.gov.companieshouse.accounts.filing.service.company.CompanyService;
import uk.gov.companieshouse.logging.Logger;

import java.util.regex.Pattern;

@Controller
@RequestMapping("/transactions/{transactionId}/accounts-filing")
public class CompanyController {
    private Logger logger;
    private CompanyService companyService;

    @Autowired
    public CompanyController(Logger logger, CompanyService companyService) {
        this.logger = logger;
        this.companyService = companyService;
    }

    @PutMapping("/company/{companyNumber}/confirm")
    public ResponseEntity<?> confirmCompany(@PathVariable final String companyNumber, @PathVariable final String transactionId){
        logger.info(String.format("Saving company_number- %s  and transaction_id- %s ",
                companyNumber, transactionId));
        if (!isRequestValid(companyNumber) || !isRequestValid(transactionId)) {
            return ResponseEntity.badRequest().build();
        }
        try {
            return ResponseEntity.ok(companyService.saveCompanyNumberAndTransactionId(companyNumber, transactionId));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * The method will check the input string is alphanumeric or not.
     *
     * @return boolean
     */
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

