package uk.gov.companieshouse.accounts.filing.model;

import org.springframework.http.ResponseEntity;

public class ConfrimCompanyResponse {

    public static ResponseEntity<?> requestNotFound() {
        return ResponseEntity.notFound().build();
    }

    public static ResponseEntity<?> badRequest() {
        return ResponseEntity.badRequest().build();
    }

    public static ResponseEntity<CompanyRecord> success(CompanyRecord confirmCompanyRecord) {
        return ResponseEntity.ok(confirmCompanyRecord);
    }

}
