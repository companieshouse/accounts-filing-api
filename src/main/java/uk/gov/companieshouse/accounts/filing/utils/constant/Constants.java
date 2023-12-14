package uk.gov.companieshouse.accounts.filing.utils.constant;

public final class Constants {

    private Constants() {}

    public static final String ERIC_REQUEST_ID_KEY = "X-Request-Id";
    public static final String TRANSACTION_ID_KEY = "transactionId";
    public static final String ACCOUNT_FILING_ID_KEY = "accountsFilingId";
    public static final String FILE_ID_KEY = "fileId";
    public static final String TRANSACTION_ALLOWED_CHAR_PATTERN = "(^[0-9-]{20}$)";
    public static final String ACCOUNTS_FILING_REGEX_PATTERN = "(^[a-z0-9]{24}$)";
    public static final String FILE_ID_REGEX_PATTERN = "(^[a-z0-9-]{36}$)";
    
}
