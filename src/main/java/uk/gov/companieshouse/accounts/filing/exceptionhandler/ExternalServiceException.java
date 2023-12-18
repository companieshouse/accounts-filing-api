package uk.gov.companieshouse.accounts.filing.exceptionhandler;

public class ExternalServiceException extends RuntimeException {
        
    private static final long serialVersionUID = 8631969689253132097L;


    public ExternalServiceException() {
        super();
    }

    public ExternalServiceException(String message) {
        super(message);
    }

    public ExternalServiceException(Throwable cause) {
        super(cause);
    }

    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
