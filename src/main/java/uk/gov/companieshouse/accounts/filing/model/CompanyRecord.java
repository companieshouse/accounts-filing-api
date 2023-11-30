package uk.gov.companieshouse.accounts.filing.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
@Document("accounts_filing")
public record CompanyRecord(@Id String id, @Field String companyNumber, @Field String transactionId){
}