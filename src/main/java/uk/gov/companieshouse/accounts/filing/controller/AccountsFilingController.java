package uk.gov.companieshouse.accounts.filing.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import uk.gov.companieshouse.accounts.filing.controller.handler.controller.exception.ControllerExceptionHandler;
import uk.gov.companieshouse.logging.Logger;

@Controller
@RequestMapping("/accounts-filing")
public class AccountsFilingController {

    private final Logger logger;

    @Autowired
    public AccountsFilingController(Logger logger) {
        this.logger = logger;
    }
    
    @GetMapping("/healthcheck")
    public ResponseEntity<String> checking(){
        return ResponseEntity.ok().body("OK");
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
