package uk.gov.companieshouse.accounts.filing.model;

import org.springframework.http.ResponseEntity;

public class ConfimCompanyResponse {

    public static ResponseEntity<?> requestNotFound() {
        return ResponseEntity.notFound().build();
    }

    public static ResponseEntity<?> badRequest() {
        return ResponseEntity.badRequest().build();
    }

    public static ResponseEntity<ConfirmCompanyRecord> success(ConfirmCompanyRecord confirmCompanyRecord) {
        return ResponseEntity.ok(confirmCompanyRecord);
    }

}
