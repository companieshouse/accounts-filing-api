package uk.gov.companieshouse.accounts.filing.exceptionhandler;

public class AmendedFilingDisabledException extends RuntimeException {

    public AmendedFilingDisabledException() {
        super("Amended accounts filing is temporarily unavailable");
    }
}
