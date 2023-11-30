package uk.gov.companieshouse.accounts.filing.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import uk.gov.companieshouse.accounts.filing.model.ConfrimCompanyResponse;
import uk.gov.companieshouse.accounts.filing.model.CompanyRecord;
import uk.gov.companieshouse.accounts.filing.repository.CompanyRespository;
import uk.gov.companieshouse.logging.Logger;

@Controller
@RequestMapping("/accounts-filing")
public class AccountsFilingController {

    private Logger logger;
    private CompanyRespository companyRespository;

    @Autowired
    public AccountsFilingController(Logger logger, CompanyRespository companyRespository) {
        this.logger = logger;
        this.companyRespository = companyRespository;
    }

    @GetMapping("/healthcheck")
    public ResponseEntity<String> checking(){
        return ResponseEntity.ok().body("OK");
    }

    @PutMapping("/company/{company_number}/confirm/{transaction_id}")
    public ResponseEntity<?> confirmCompany(@PathVariable("company_number") String companyNumber, @PathVariable("transaction_id")  String transactionId){
        logger.info(String.format("Saving company number %s",
                companyNumber));
        if (transactionId == null || transactionId.isBlank()) {
            return ConfrimCompanyResponse.badRequest();
        }
        try {
            CompanyRecord companyRecord = new CompanyRecord(null,companyNumber,transactionId);
           return ConfrimCompanyResponse.success(companyRespository.save(companyRecord));
        } catch(Exception ex) {
            return ConfrimCompanyResponse.requestNotFound();
        }
    }
}
