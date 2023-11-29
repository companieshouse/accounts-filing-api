package uk.gov.companieshouse.accounts.filing.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import uk.gov.companieshouse.accounts.filing.model.FilingRecord;
import uk.gov.companieshouse.accounts.filing.model.FilingRequest;
import uk.gov.companieshouse.accounts.filing.model.FilingResponse;
import uk.gov.companieshouse.accounts.filing.repository.AccountsFilingRepository;
import uk.gov.companieshouse.logging.Logger;

@Controller
@RequestMapping("/accounts-filing")
public class AccountsFilingController {

    private Logger logger;
    private AccountsFilingRepository accountsFilingRepository;

    @Autowired
    public AccountsFilingController(Logger logger,AccountsFilingRepository accountsFilingRepository) {
        this.logger=logger;
        this.accountsFilingRepository=accountsFilingRepository;
    }

    @GetMapping("/check")
    public ResponseEntity<?> checking(){
        return ResponseEntity.ok().body("it is just for testing");

    }

    @PutMapping("/company/{company_number}/confirm")
    public ResponseEntity<?> updateCompanyConfirmation(@PathVariable("company_number") int companyNumber, @RequestBody FilingRequest filingRequest){
        logger.info(String.format(
                "Processing company filing records for company number %s",
                companyNumber));
        if (filingRequest == null || filingRequest.getTransactionId().isEmpty()) {
            return FilingResponse.badRequest();
        }
        try {
            var insertedFilingRecord = accountsFilingRepository.insert(new FilingRecord());
            var record =  accountsFilingRepository.save(new FilingRecord(insertedFilingRecord.getId(), companyNumber, filingRequest.getTransactionId()));
            return FilingResponse.success(record);
        }catch(Exception ex){
            return FilingResponse.requestNotFound();
        }
    }
}
