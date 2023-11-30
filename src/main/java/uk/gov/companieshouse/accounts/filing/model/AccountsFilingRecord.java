package uk.gov.companieshouse.accounts.filing.model;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import org.springframework.data.annotation.Id;

@Document("accounts_filing")
public record AccountsFilingRecord(
    @Id String accountFilingId,
    @Field String fileId,
    @Field String accountType
){
    public static AccountsFilingRecord validateResult(String accountFilingId, String fileId, String accountType){
        return new AccountsFilingRecord(accountFilingId, fileId, accountType);
    }
}
