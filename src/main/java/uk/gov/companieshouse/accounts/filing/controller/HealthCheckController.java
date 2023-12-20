package uk.gov.companieshouse.accounts.filing.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/accounts-filing")
public class HealthCheckController {
    
    @GetMapping("/healthcheck")
    public ResponseEntity<String> checking(){
        return ResponseEntity.ok().body("OK");
    }
}
