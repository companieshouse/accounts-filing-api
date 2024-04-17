package uk.gov.companieshouse.accounts.filing.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import uk.gov.companieshouse.accounts.filing.controller.handler.controller.exception.ControllerExceptionHandler;
import uk.gov.companieshouse.accounts.filing.model.CompanyRequest;
import uk.gov.companieshouse.accounts.filing.service.company.CompanyService;
import uk.gov.companieshouse.logging.Logger;

import java.util.regex.Pattern;

@Controller
@RequestMapping("/transactions/{transactionId}/accounts-filing")
public class CompanyController {
    private final Logger logger;
    private final CompanyService companyService;

    @Autowired
    public CompanyController(Logger logger, CompanyService companyService) {
        this.logger = logger;
        this.companyService = companyService;
    }

    @PutMapping("/company/{companyNumber}/confirm")
    public ResponseEntity<?> confirmCompany(@PathVariable("companyNumber") final String companyNumber,
                                            @PathVariable("transactionId") final String transactionId,
                                            @Valid @RequestBody final CompanyRequest companyRequest){
        logger.info(String.format("Saving company_number- %s, transaction_id- %s and company name - %s",
                companyNumber, transactionId, companyRequest.companyName()));
        if (!checkCompanyNumber(companyNumber) || !checkTransactionId(transactionId)) {
            return ResponseEntity.badRequest().build();
        }
        try {
            return ResponseEntity.ok(companyService.saveCompanyNumberAndTransactionId(companyNumber, transactionId, companyRequest));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * The method will check the passed company number is in correct
     * charactor set or not. If it is using the correct charactor set, the method will return true
     * else return false.
     *
     * @return boolean
     */
    private boolean checkCompanyNumber(String companyNumber) {
        if (companyNumber == null || companyNumber.isBlank()) {
            return false;
        } else {
            Pattern pattern = Pattern.compile("^[A-Z0-9]{8}$");
            return pattern.matcher(companyNumber).matches();
        }
    }

    /**
     * The method will check the passed transaction id is in correct
     * format or not. If it is in correct format, the method will return true
     * else return false.
     *
     * @return boolean
     */
    private boolean checkTransactionId(String transactionId) {
        if (transactionId == null || transactionId.isBlank()) {
            return false;
        } else {
            Pattern pattern = Pattern.compile("^([0-9]{6}-[0-9]{6}-[0-9]{6})$");
            return pattern.matcher(transactionId).matches();
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

