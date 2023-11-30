package uk.gov.companieshouse.accounts.filing.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import uk.gov.companieshouse.accounts.filing.model.ConfimCompanyResponse;
import uk.gov.companieshouse.accounts.filing.model.ConfirmCompanyRecord;
import uk.gov.companieshouse.accounts.filing.repository.AccountsFilingRepository;
import uk.gov.companieshouse.logging.Logger;

@Controller
@RequestMapping("/accounts-filing")
public class AccountsFilingController {

    private Logger logger;
    private AccountsFilingRepository accountsFilingRepository;

    @Autowired
    public AccountsFilingController(Logger logger, AccountsFilingRepository accountsFilingRepository) {
        this.logger = logger;
        this.accountsFilingRepository = accountsFilingRepository;
    }

    @GetMapping("/check")
    public ResponseEntity<?> checking(){
        return ResponseEntity.ok().body("it is just for testing");

    }

    @PutMapping("/company/{company_number}/confirm/{transaction_id}")
    public ResponseEntity<?> confirmCompany(@PathVariable("company_number") String companyNumber, @PathVariable("transaction_id")  String transactionId){
        logger.info(String.format("Saving company number %s",
                companyNumber));
        if (transactionId == null || transactionId.isBlank()) {
            return ConfimCompanyResponse.badRequest();
        }
        try {
            ConfirmCompanyRecord companyRecord = new ConfirmCompanyRecord(null,companyNumber,transactionId);
           return ConfimCompanyResponse.success(accountsFilingRepository.save(companyRecord));
        } catch(Exception ex) {
            return ConfimCompanyResponse.requestNotFound();
        }
    }
}
