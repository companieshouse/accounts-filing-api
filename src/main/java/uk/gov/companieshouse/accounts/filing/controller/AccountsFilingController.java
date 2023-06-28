package uk.gov.companieshouse.accounts.filing.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/accounts-filing")
public class AccountsFilingController {

    @GetMapping("/check")
    public ResponseEntity<?> checking(){
        return ResponseEntity.ok().body("it is just for testing");

    }
}
