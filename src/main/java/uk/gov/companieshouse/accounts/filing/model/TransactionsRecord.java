package uk.gov.companieshouse.accounts.filing.model;
public record TransactionsRecord(
    String accountFilingId,
    String fileId,
    String accountType
){
    public static TransactionsRecord validateResult(String accountFilingId, String fileId, String accountType){
        return new TransactionsRecord(accountFilingId, fileId, accountType);
    }
}
