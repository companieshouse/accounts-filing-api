package uk.gov.companieshouse.accounts.filing.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document("accounts_filing")
public record AccountsFilingRecord(
        @Id String id, @Field CompanyRecord companyRecord, @Field TransactionsRecord record) {

    public static AccountsFilingRecord validateResult(String id, CompanyRecord companyRecord,TransactionsRecord record){
        return new AccountsFilingRecord(id,companyRecord, record);
    }
}
