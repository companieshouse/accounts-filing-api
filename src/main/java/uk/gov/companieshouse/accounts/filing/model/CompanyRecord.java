package uk.gov.companieshouse.accounts.filing.model;
public record CompanyRecord(
        String companyNumber,
        String transactionId){
    public static CompanyRecord validateResult(String companyNumber, String transactionId){
        return new CompanyRecord(companyNumber, transactionId);
    }
}