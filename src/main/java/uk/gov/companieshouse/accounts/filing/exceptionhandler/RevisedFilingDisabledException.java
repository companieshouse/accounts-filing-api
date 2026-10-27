package uk.gov.companieshouse.accounts.filing.exceptionhandler;

public class RevisedFilingDisabledException extends RuntimeException {



    public RevisedFilingDisabledException() {
        super("Revised accounts filing is temporarily unavailable");
    }
}
