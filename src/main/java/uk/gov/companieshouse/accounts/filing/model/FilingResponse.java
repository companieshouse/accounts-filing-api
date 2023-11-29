package uk.gov.companieshouse.accounts.filing.model;

import org.springframework.http.ResponseEntity;

public class FilingResponse {

    public static ResponseEntity<?> requestNotFound() {
        return ResponseEntity.notFound().build();
    }

    public static ResponseEntity<?> badRequest() {
        return ResponseEntity.badRequest().build();
    }

    public static ResponseEntity<FilingRecord> success(FilingRecord filingRecord) {
        return ResponseEntity.ok(filingRecord);
    }

}
