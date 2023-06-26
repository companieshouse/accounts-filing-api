package uk.gov.companieshouse.account.filing.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/account-filing/filing")
public class AccountFilingController {
    @GetMapping("/check")
    ResponseEntity<?> checking() {
        return ResponseEntity.ok().body("Testing complete");
    }

}

