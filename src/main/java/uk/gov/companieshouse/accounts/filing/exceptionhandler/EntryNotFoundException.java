package uk.gov.companieshouse.accounts.filing.exceptionhandler;

public class EntryNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 8631969689253132097L;


    public EntryNotFoundException() {
        super();
    }

    public EntryNotFoundException(String message) {
        super(message);
    }

    public EntryNotFoundException(Throwable cause) {
        super(cause);
    }

    public EntryNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
